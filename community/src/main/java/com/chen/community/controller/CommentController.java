package com.chen.community.controller;

import com.chen.community.entity.Comment;
import com.chen.community.entity.DiscussPost;
import com.chen.community.entity.Event;
import com.chen.community.entity.User;
import com.chen.community.event.EventProducer;
import com.chen.community.service.CommentService;
import com.chen.community.service.DiscussPostService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.HostHolder;
import com.chen.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        User user = hostHolder.getUser();
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT) // 设置触发事件的主题，这里是评论，即有人给某个帖子或者评论进行评论后就要触发系统通知
                .setUserId(hostHolder.getUser().getId()) // 触发事件的用户
                .setEntityId(comment.getEntityId()) // 触发目标实体id
                .setEntityType(comment.getEntityType()) // 触发目标实体类型
                .setData("postId", discussPostId); // 因为用户收到通知后可以点击查看，点击后要跳到该帖子的页面，因此要知道这个帖子的id
        // 如果是给帖子评论，那么event中的EntityUserId是帖子的发布人
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost discussPost = discussPostService.selectDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPost.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) { // 如果是给评论进行评论
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event); // 发布消息，然后消费者去消费即可

        // 如果是给帖子进行评论，那么将新增了评论的帖子更新到ES服务器中，覆盖原来的帖子。
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLIC)
                    .setUserId(comment.getId())
                    .setEntityId(comment.getEntityId())
                    .setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
