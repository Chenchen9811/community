package com.chen.community.service.impl;

import com.chen.community.dao.EntityLikeMapper;
import com.chen.community.entity.EntityLike;
import com.chen.community.service.EntityLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityLikeServiceImpl implements EntityLikeService {
    @Autowired
    private EntityLikeMapper entityLikeMapper;

    @Override
    public int addEntityLike(EntityLike entityLike) {
        return entityLikeMapper.insertEntityLike(entityLike);
    }

    @Override
    public int updateEntityLikeStatus(int userId, int entityId, int entityType, int status) {
        return entityLikeMapper.updateEntityLikeStatus(userId, entityId, entityType, status);
    }

    @Override
    public EntityLike findEntityLike(int userId, int entityId, int entityType) {
        return entityLikeMapper.selectEntityLike(userId, entityId, entityType);
    }

    @Override
    public int findEntityLikeCount(int entityId, int entityType) {
        return entityLikeMapper.selectEntityLikeCount(entityId, entityType);
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityId, int entityType) {
        return entityLikeMapper.selectEntityLikeStatus(userId, entityId, entityType);
    }

    @Override
    public List<Integer> findUserIdsByEntityTypeAndEntityId(int entityId, int entityType) {
        return entityLikeMapper.selectUserIdsByEntityTypeAndEntityId(entityId, entityType);
    }
}
