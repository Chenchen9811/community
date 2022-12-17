package com.chen.community.util;

import com.chen.community.controller.UserController;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWordsFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordsFilter.class);

    // 替换的内容
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 根据敏感词内容初始化前缀树
    @PostConstruct
    public void init() {
        try (
                // 获取敏感词
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filteredWords(String text) {
        //若是空字符串 返回空
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 根节点
        // 每次在目标字符串中找到一个敏感词，完成替换之后，都要再次从根节点遍历树开始一次新的过滤
        TrieNode tempNode = rootNode;
        // begin指针作用是目标字符串每次过滤的开头
        int begin = 0;
        // position指针的作用是指向待过滤的字符
        // 若position指向的字符是敏感词的结尾，那么text.subString(begin,position+1)就是一个敏感词
        int position = 0;
        //过滤后的结果
        StringBuilder result = new StringBuilder();

        //开始遍历 position移动到目标字符串尾部是 循环结束
        while (position < text.length()) {
            // 最开始时begin指向0 是第一次过滤的开始
            // position也是0
            char c = text.charAt(position);

            //忽略用户故意输入的符号 例如嫖※娼 忽略※后 前后字符其实也是敏感词
            if (isSymbol(c)) {
                //判断当前节点是否为根节点
                //若是根节点 则代表目标字符串第一次过滤或者目标字符串中已经被遍历了一部分
                //因为每次过滤掉一个敏感词时，都要将tempNode重新置为根节点,以重新去前缀树中继续过滤目标字符串剩下的部分
                //因此若是根节点，代表依次新的过滤刚开始，可以直接将该特殊符号字符放入到结果字符串中
                if (tempNode == rootNode) {
                    //将用户输入的符号添加到result中
                    result.append(c);
                    //此时将单词begin指针向后移动一位，以开始新的一个单词过滤
                    begin++;
                }
                //若当前节点不是根节点，那说明符号字符后的字符还需要继续过滤
                //所以单词开头位begin不变化，position向后移动一位继续过滤
                position++;
                continue;
            }
            //判断当前节点的子节点是否有目标字符c
            tempNode = tempNode.getSubNode(c);
            //如果没有 代表当前beigin-position之间的字符串不是敏感词
            // 但begin+1-position却不一定不是敏感词
            if (tempNode == null) {
                //所以只将begin指向的字符放入过滤结果
                result.append(text.charAt(begin));
                //position和begin都指向begin+1
                position = ++begin;
                //再次过滤
                tempNode = rootNode;
            } else if (tempNode.isSensitiveWordEnd()) {
                //如果找到了子节点且子节点是敏感词的结尾
                //则当前begin-position间的字符串是敏感词 将敏感词替换掉
                result.append(REPLACEMENT);
                //begin移动到敏感词的下一位
                begin = ++position;
                //再次过滤
                tempNode = rootNode;
                //&& begin < position - 1
            } else if (position + 1 == text.length()) {
                //特殊情况
                //虽然position指向的字符在树中存在，但不是敏感词结尾，并且position到了目标字符串末尾（这个重要）
                //因此begin-position之间的字符串不是敏感词 但begin+1-position之间的不一定不是敏感词
                //所以只将begin指向的字符放入过滤结果
                result.append(text.charAt(begin));
                //position和begin都指向begin+1
                position = ++begin;
                //再次过滤
                tempNode = rootNode;
            } else {
                //position指向的字符在树中存在，但不是敏感词结尾，并且position没有到目标字符串末尾
                position++;
            }
        }
        return begin < text.length() ? result.append(text.substring(begin)).toString() : result.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0X9FFF 是东亚文字范围
        return CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0X9FFF);
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        char[] chars = keyword.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; i ++) {
            char c = chars[i];
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮
            tempNode = subNode;
            // 设置结束标识
            if (i == len - 1) tempNode.setSensitiveWordEnd(true);
        }
    }

    // 前缀树
    private class TrieNode {
        // 关键词结束表示
        private boolean isSensitiveWordEnd = false;

        // 子节点（key是下级节点的字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isSensitiveWordEnd() {
            return isSensitiveWordEnd;
        }

        public void setSensitiveWordEnd(boolean sensitiveWordEnd) {
            isSensitiveWordEnd = sensitiveWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }
    }
}
