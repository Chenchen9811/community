package com.chen.community.service.impl;

import com.chen.community.dao.CommentMapper;
import com.chen.community.dao.DiscussPostMapper;
import com.chen.community.entity.Comment;
import com.chen.community.service.CommentService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.SensitiveWordsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        if (comment == null) {
            throw  new IllegalArgumentException("参数不能为空！");
        }
        // 过滤评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordsFilter.filteredWords(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        // 更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostMapper.updateDiscussPostCommentCount(comment.getEntityId(), count);
        }
        return 0;
    }

    @Override
    public Comment findCommentById(int id) {
        return commentMapper.SelectCommentById(id);
    }
}
