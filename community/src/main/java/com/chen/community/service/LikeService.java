package com.chen.community.service;

public interface LikeService {

    /**
     * 给实体点赞
     * @param userId 点赞的用户id
     * @param entityType 点赞的实体（给帖子点赞还是给评论点赞）
     * @param entityId 点赞的实体的id
     * @param entityUserId 该实体所属的用户id
     */
    public void like(int userId, int entityType, int entityId ,int entityUserId);

    /**
     * 查询某个实体（帖子、评论）的点赞数量
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId);

    /**
     * 查看某用户是否对该实体（帖子、评论）点过赞
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 1 - 点过赞; 0 - 没点过
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId);

    /**
     * 查询某个用户收到的点赞数量
     * @param userId 用户id
     * @return
     */
    public int findUserLikeCount(int userId);
}
