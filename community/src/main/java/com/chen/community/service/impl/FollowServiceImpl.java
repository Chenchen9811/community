package com.chen.community.service.impl;

import com.chen.community.entity.User;
import com.chen.community.service.FollowService;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 用户关注某个实体
     *
     * @param userId     关注人
     * @param entityType 关注的实体
     * @param entityId   关注的实体id
     */
    @Override
    public void follow(int userId, int entityType, int entityId) {
        // 思路：关注的时候要做两次存储，一个是某人关注了什么，另一个是被关注实体要增加谁关注了它
        // 所以要保证事务。
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();
                // 用户id为userId的用户关注了entityType实体。
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // entityType实体被用户id为userId的用户关注了
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    /**
     * 用户取关某个实体
     *
     * @param userId     取关的用户id
     * @param entityType 取关的实体
     * @param entityId   取关的实体id
     */
    @Override
    public void unFollow(int userId, int entityType, int entityId) {
        // 思路：关注的时候要做两次存储，一个是某人取关了什么，另一个是被关注实体要删除取关的用户
        // 所以要保证事务。
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();
                // 用户id为userId的用户取关了entityType实体。
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                // entityType实体删除用户id为userId的用户
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某个用户关注的实体的数量，比如关注了多少个用户、关注了多少个帖子
     *
     * @param userId     这个用户的id
     * @param entityType 要查询这个用户关注的实体类型（关注了用户还是帖子）
     * @return
     */
    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询实体的粉丝数量
     *
     * @param entityType 实体类型（用户、帖子）
     * @param entityId   实体id
     * @return
     */
    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    /**
     * 查询当前用户是否已关注该实体
     *
     * @param userId     当前用户id
     * @param entityType 要查询的实体类型
     * @param entityId   实体id
     * @return
     */
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        // 思路：从当前用户的关注目标中判断是否有该实体即可。
        // 即判断followee:userId:entityType 中该entityId是否存在。
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 查询这个entityId是否有score来判断存不存在。
        Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
        return score != null;
    }

    /**
     * 查询某用户关注的人(当前点击的个人主页中的用户)
     *
     * @param userId 用户id
     * @param offset 分页起始行
     * @param limit  每页显示的行数
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        // key：  followee:userId:entityType(3)
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 按照关注时间倒叙查询，zset是一个有序集合，这里存进去的数据是以创建时间作为分数来排序的。
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.selectUserByUserId(targetId);
            // 存入目标用户
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    /**
     * 查询有多少个用户关注了当前用户（当前点击的个人主页中的用户）（支持分页）
     *
     * @param userId 当前用户
     * @param offset 分页查询起始行
     * @param limit  每页显示的行数
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        // followerKey:  follower:entityType:entityId -> zset(userId, now)
        // 在这里就是 follower:3(实体是人):userId -> zset(userId, now)  表示有zset(value)中的xx（数量）人关注了id为userId（key中的userId）的用户。
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (followerIds == null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer followerId : followerIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.selectUserByUserId(followerId);
            map.put("user", user);
            // 查询关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
