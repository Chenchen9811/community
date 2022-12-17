package com.chen.community.service;


import com.chen.community.entity.Comment;

import java.util.List;

public interface CommentService {

    /**
     *  根据目标实体（帖子、评论、课程等）查找评论，也就是可以对帖子进行评论，也能对评论进行评论。
     * @param entityType 目标实体类型
     * @param entityId 目标实体类型中的id
     * @param offset 起始行
     * @param limit 分页中每页显示的行数
     * @return
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据实体查询该实体的评论数
     * @param entityType 目标实体类型
     * @param entityId 目标实体类型中的id
     * @return
     */
    public int findCommentCount(int entityType, int entityId);


    /**
     * 增加评论
     * @param comment
     * @return
     */
    public int addComment(Comment comment);

    /**
     * 根据id查comment
     * @param id
     * @return
     */
    Comment findCommentById(int id);
}
