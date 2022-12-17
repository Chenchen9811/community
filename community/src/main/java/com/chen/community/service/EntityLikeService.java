package com.chen.community.service;

import com.chen.community.entity.EntityLike;

import java.util.List;

public interface EntityLikeService {

    /**
     * 增加一条点赞记录
     * @param entityLike
     * @return
     */
    int addEntityLike(EntityLike entityLike);

    /**
     * 修改点赞状态
     * @param userId 点赞的用户
     * @param entityId 被点赞的实体id
     * @param entityType 被点赞的实体类型
     * @param status 要修改的点赞状态 0-未点赞 1-已点赞
     * @return
     */
    int updateEntityLikeStatus(int userId, int entityId, int entityType, int status);


    /**
     * 查询点赞记录
     * @param userId 点赞id
     * @param entityId 被点赞的实体id
     * @param entityType 被点赞的实体类型
     * @return
     */
    EntityLike findEntityLike(int userId, int entityId, int entityType);

    /**
     * 查询某个实体收到的点赞量
     * @param entityId
     * @param entityType
     * @return
     */
    int findEntityLikeCount(int entityId, int entityType);

    /**
     * 获取点赞状态
     * @param userId 点赞id
     * @param entityId 被点赞的实体id
     * @param entityType 被点赞的实体类型
     * @return
     */
    int findEntityLikeStatus(int userId, int entityId, int entityType);

    /**
     * 获取所有对某个实体点赞的用户id
     * @param entityId
     * @param entityType
     * @return
     */
    List<Integer> findUserIdsByEntityTypeAndEntityId(int entityId, int entityType);
}
