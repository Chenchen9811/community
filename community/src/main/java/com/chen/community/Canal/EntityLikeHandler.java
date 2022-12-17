package com.chen.community.Canal;

import com.chen.community.entity.EntityLike;
import com.chen.community.entity.Event;
import com.chen.community.event.EventProducer;
import com.chen.community.service.DiscussPostService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@CanalTable("entity_like")
@Component
public class EntityLikeHandler implements EntryHandler<EntityLike>, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EntityLikeHandler.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Override
    public void update(EntityLike before, EntityLike after) {
        logger.info("订阅到binlog日志发生变化，开始执行删除缓存操作");
        Integer entityType = before.getEntityType();
        Integer entityId = before.getEntityId();
        Integer status = before.getStatus();
        if (entityType != null && entityId != null && status == ENTITY_LIKE_STATUS_UNLIKE) {
            String entityLikeKey = RedisKeyUtil.getEntityLikeKey(before.getEntityType(), before.getEntityId());
            int userId = discussPostService.findUserIdByDiscussPostId(before.getEntityId());
            String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
            redisTemplate.opsForSet().remove(entityLikeKey);
            redisTemplate.opsForValue().decrement(userLikeKey);
            checkIsDeleted(entityType, entityId);
        } else {
            String entityLikeKey = RedisKeyUtil.getEntityLikeKey(before.getEntityType(), before.getEntityId());
            int userId = discussPostService.findUserIdByDiscussPostId(before.getEntityId());
            String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
            redisTemplate.opsForSet().remove(entityLikeKey);
            redisTemplate.opsForValue().increment(userLikeKey);
            checkIsDeleted(entityType, entityId);
        }
    }

    @Override
    public void insert(EntityLike entityLike) {
        System.out.println("detect insert method");
    }

    @Override
    public void delete(EntityLike entityLike) {
        System.out.println("detect delete method");
    }

    private void checkIsDeleted(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if (!redisTemplate.hasKey(entityLikeKey)) {
            Event event = new Event()
                    .setTopic(TOPIC_DELETE_CACHE)
                    .setEntityId(entityId)
                    .setEntityType(entityType);
            eventProducer.fireEvent(event);
        }
    }
}
