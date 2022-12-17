package com.chen.community.service;


import com.chen.community.entity.Page;
import com.chen.community.entity.User;

import java.util.List;
import java.util.Map;

public interface FollowService {



    /**
     * 用户关注某个实体
     * @param userId 关注人
     * @param entityType 关注的实体
     * @param entityId 关注的实体id
     */
    public void follow(int userId, int entityType, int entityId);

    /**
     * 用户取关某个实体
     * @param userId 取关的用户id
     * @param entityType 取关的实体
     * @param entityId 取关的实体id
     */
    public void unFollow(int userId, int entityType, int entityId);



    /**
     * 查询某个用户关注的实体的数量，比如关注了多少个用户、关注了多少个帖子
     * @param userId 这个用户的id
     * @param entityType 要查询这个用户关注的实体类型（关注了用户还是帖子）
     * @return
     */
    public long findFolloweeCount(int userId, int entityType);


    /**
     * 查询实体的粉丝数量
     * @param entityType 实体类型（用户、帖子）
     * @param entityId 实体id
     * @return
     */
    public long findFollowerCount(int entityType, int entityId);


    /**
     * 查询当前用户是否已关注该实体
     * @param userId 当前用户id
     * @param entityType 要查询的实体类型
     * @param entityId 实体id
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId);

    /**
     * 查询某用户关注的人（支持分页）
     * @param userId 用户id
     * @param offset 分页查询起始行
     * @param limit 每页显示的行数
     * @return
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit);

    /**
     * 查询有多少个用户关注了当前用户（支持分页）
     * @param userId 当前用户
     * @param offset 分页查询起始行
     * @param limit 每页显示的行数
     * @return
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit);
}
