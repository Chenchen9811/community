package com.chen.community.service.impl;

import com.chen.community.controller.DiscussPostController;
import com.chen.community.dao.DiscussPostMapper;
import com.chen.community.dao.UserMapper;
import com.chen.community.entity.DiscussPost;
import com.chen.community.entity.User;
import com.chen.community.service.DiscussPostService;
import com.chen.community.util.SensitiveWordsFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expired-seconds}")
    private int expireSeconds;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.info("load discussPostList from DB.");
        // DiscussPost中的userId是外键，在网页中不需要显示，网页中要显示的是这个帖子的发表人username。
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }
    // Caffeine核心接口：Cache，LoadingCache（支持锁，当缓存中没有数据时就上锁，阻塞后面的请求，取到数据后再释放锁，本项目使用这个）， AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        // 获取key的参数， offset:limit，用":"分割
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);
                        // 在访问数据库前可以访问redis，redis中没有再访问mysql。
                        logger.info("load discussPostList from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(@NonNull Integer integer) throws Exception {
                        if (integer != 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        // 在查询数据库之前可以先查询redis
                        logger.info("load discussPostRows from DB.");
                        return discussPostMapper.selectAllDiscussPostRows(integer);
                    }
                });
    }

    @Override
    public int findDiscussPostRows(int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.info("load discussPostRows from DB.");
        return discussPostMapper.selectAllDiscussPostRows(userId);
    }

    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveWordsFilter.filteredWords(discussPost.getTitle()));
        discussPost.setContent(sensitiveWordsFilter.filteredWords(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateDiscussPostCommentCount(int id, int commentCount) {
        return discussPostMapper.updateDiscussPostCommentCount(id, commentCount);
    }

    /**
     * 修改帖子类型
     * @param id 帖子id
     * @param type 要修改的类型
     * @return
     */
    @Override
    public int updateDiscussPostType(int id, int type) {
        return discussPostMapper.updateDiscussPostType(id, type);
    }

    /**
     *
     * @param id 帖子id
     * @param status 要修改的状态
     * @return
     */
    @Override
    public int updateDiscussPostStatus(int id, int status) {
        return discussPostMapper.updateDiscussPostStatus(id, status);
    }

    @Override
    public int updateDiscussPostScore(int id, double score) {
        return discussPostMapper.updateDiscussPostScore(id, score);
    }

    @Override
    public int findUserIdByDiscussPostId(int discussPostId) {
        return discussPostMapper.selectUserIdByDiscussPostId(discussPostId);
    }
}
