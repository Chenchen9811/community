package com.chen.community.controller;

import com.chen.community.entity.Event;
import com.chen.community.entity.User;
import com.chen.community.event.EventProducer;
import com.chen.community.service.LikeService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.HostHolder;
import com.chen.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 给实体（帖子，或者评论）点赞
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 获取当前用户
        User user = hostHolder.getUser();
        // 点赞，双重作用，点第一次是点赞，点第二次是取消赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 统计当前实体的点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 查询当前用户对该实体的点赞状态（是点赞状态还是取消点赞状态）
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        //logger.info("点赞数{}", likeCount);
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件，发送通知，点赞才发送通知，取消点赞则不用通知
        if (likeCount == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    // 用户收到点赞通知后，可以查看是对哪个帖子的点赞，因此这里要传入帖子id，方便拼接帖子的URL
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        // 如果是给帖子点赞
        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }
}

