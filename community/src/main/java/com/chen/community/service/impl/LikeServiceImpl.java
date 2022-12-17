package com.chen.community.service.impl;

import com.chen.community.dao.EntityLikeMapper;
import com.chen.community.entity.EntityLike;
import com.chen.community.service.CommentService;
import com.chen.community.service.DiscussPostService;
import com.chen.community.service.EntityLikeService;
import com.chen.community.service.LikeService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.RedisKeyUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class LikeServiceImpl implements LikeService, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EntityLikeService entityLikeService;

    @Override
    @Transactional
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 查询有没有这条点赞记录
        EntityLike selectEntityLike = entityLikeService.findEntityLike(userId, entityId, entityType);
        // 给某个实体（评论、帖子）点赞后，该实体所属的用户收到的赞也要+1.所以要事务执行
        EntityLike userReceivedLike = entityLikeService.findEntityLike(userId, entityUserId, ENTITY_TYPE_USER);
        // 先写数据库
        if (selectEntityLike == null && userReceivedLike == null) {
            EntityLike entityLike = new EntityLike();
            entityLike.setEntityType(entityType);
            entityLike.setEntityId(entityId);
            entityLike.setUserId(userId);
            entityLike.setStatus(ENTITY_LIKE_STATUS_LIKE);
            entityLike.setCreateTime(new Date());
            entityLikeService.addEntityLike(entityLike);
            entityLike.setEntityId(entityUserId);
            entityLike.setEntityType(ENTITY_TYPE_USER);
            entityLikeService.addEntityLike(entityLike);
        } else {
            // 获取点赞状态
            int likeStatus = entityLikeService.findEntityLikeStatus(userId, entityId, entityType);
            if (likeStatus == ENTITY_LIKE_STATUS_LIKE) {
                // 对评论或者帖子点赞时，同时将评论或者帖子所属的
                entityLikeService.updateEntityLikeStatus(userId, entityId, entityType, ENTITY_LIKE_STATUS_UNLIKE);
                entityLikeService.updateEntityLikeStatus(userId, entityUserId, ENTITY_TYPE_USER, ENTITY_LIKE_STATUS_UNLIKE);
            } else {
                entityLikeService.updateEntityLikeStatus(userId, entityId, entityType, ENTITY_LIKE_STATUS_LIKE);
                entityLikeService.updateEntityLikeStatus(userId, entityUserId, ENTITY_TYPE_USER, ENTITY_LIKE_STATUS_LIKE);
            }
        }
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
//        // 再删缓存
//        redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        redisTemplate.opsForValue().decrement(userLikeKey);


        // 原来：直接在redis上增和删，获取也是直接从redis上获取。带来的问题：数据多后内存会不够放。
//        redisTemplate.execute(new SessionCallback() {
//            @Override
//            public Object execute(RedisOperations redisOperations) throws DataAccessException {
//                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//                // 找到被点赞实体的所属用户，前端传来的
//                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
//                // 在事务执行开始之前做查询，因为redis在事务执行的过程中执行查询语句的话返回是空
//                Boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
//
//                redisOperations.multi(); // 开始事务
//                if (isMember) { // 如果不是第一次点赞，那么取消点赞，对用户的点赞也是同样的处理逻辑。
//                    redisOperations.opsForSet().remove(entityLikeKey, userId);
//                    redisOperations.opsForValue().decrement(userLikeKey);
//                } else {
//                    redisOperations.opsForSet().add(entityLikeKey, userId);
//                    redisOperations.opsForValue().increment(userLikeKey);
//                }
//                return redisOperations.exec(); // 执行事务
//            }
//        });
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 缓存没查到那么直接去db查。
        if (redisTemplate.hasKey(entityLikeKey)) {
            return redisTemplate.opsForSet().size(entityLikeKey);
        } else {
            // db中查到将数据写入缓存。
            List<Integer> userIds = entityLikeService.findUserIdsByEntityTypeAndEntityId(entityId, entityType);
            for (int userId: userIds) {
                redisTemplate.opsForSet().add(entityLikeKey, userId);
            }
            return userIds.size();
        }
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 缓存没查到那么直接去db查。
        if (redisTemplate.hasKey(entityLikeKey)) {
            return redisTemplate.opsForSet().isMember(entityLikeKey, userId)? 1 : 0;
        }else {
            // db中查到将数据写入缓存。
            List<Integer> userIds = entityLikeService.findUserIdsByEntityTypeAndEntityId(entityId, entityType);
            for (int user_Id: userIds) {
                redisTemplate.opsForSet().add(entityLikeKey, user_Id);
            }
            return userIds.contains(userId)? 1 : 0;
        }
    }

    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        if (redisTemplate.hasKey(userLikeKey)) {
            Integer integer = (Integer)redisTemplate.opsForValue().get(userLikeKey);
            return  integer == null? 0 : integer.intValue();
        }
        else {
            int userLikeCount = entityLikeService.findEntityLikeCount(userId, ENTITY_TYPE_USER);
            redisTemplate.opsForValue().increment(userLikeKey);
            return userLikeCount;
        }
    }

}
