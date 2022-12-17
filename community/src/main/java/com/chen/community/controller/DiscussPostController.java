package com.chen.community.controller;

import com.chen.community.entity.*;
import com.chen.community.event.EventProducer;
import com.chen.community.service.*;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.HostHolder;
import com.chen.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);


    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        // 触发发帖事件，将发布的帖子存到ES服务器中
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost discussPost = discussPostService.selectDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);

        // 帖子作者
        User user = userService.selectUserByUserId(discussPost.getUserId());
        // 帖子点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态（未登录的时候默认是没点赞）
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //logger.info("user:" + user);
        model.addAttribute("user", user);

        // 评论查询与分页显示
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());
        // 评论：给帖子的评论
        // 回复：给评论的评论
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        // 评论的Vo列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) { // 遍历每个评论
            for (Comment comment : commentList) {
                // 一个评论的Vo
                Map<String, Object> commentVo = new HashMap<>();
                // 评论内容
                commentVo.put("comment", comment);
                // 评论作者
                commentVo.put("user", userService.selectUserByUserId(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态（未登录的时候默认是没点赞）
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表（评论的评论）
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);// 有多少条查多少条，即就不分页
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) { // 遍历每个回复
                        Map<String, Object> replyVo = new HashMap<>();
                        // 一个回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.selectUserByUserId(reply.getUserId()));
                        // 回复的目标
                        User target = reply.getTargetId() == 0? null : userService.selectUserByUserId(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态（未登录的时候默认是没点赞）
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    // 将帖子置顶（异步请求，点击按钮后不整体刷新）
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateDiscussPostType(id, 1); //置顶
        // 更新完帖子类型后触发发帖事件，方便elasticsearch能搜索到
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 将帖子加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateDiscussPostStatus(id, 1); // 加精
        // 更新完帖子状态后触发发帖事件，方便elasticsearch能搜索到
        Event event = new Event()
                .setTopic(TOPIC_PUBLIC)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);
        return CommunityUtil.getJSONString(0);
    }

    // 将帖子删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteDiscusPost(int id) {
        discussPostService.updateDiscussPostStatus(id, 2); // 删除
        // 删除后触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
