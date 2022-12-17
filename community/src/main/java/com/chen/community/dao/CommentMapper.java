package com.chen.community.dao;

import com.chen.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     *  根据目标实体（帖子、评论、课程等）查找评论，也就是可以对帖子进行评论，也能对评论进行评论。
     * @param entityType 目标实体类型
     * @param entityId 目标实体类型中的id
     * @param offset 起始行
     * @param limit 分页中每页显示的行数
     * @return
     */
    public List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 查询评论条数，为分页做准备
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 增加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     * 根据id查comment
     * @param id
     * @return
     */
    Comment SelectCommentById(int id);
}
