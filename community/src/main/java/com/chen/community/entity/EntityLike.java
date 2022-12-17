package com.chen.community.entity;


import top.javatool.canal.client.annotation.CanalTable;

import javax.annotation.Nullable;
import java.util.Date;
public class EntityLike {

    private Integer id;

    private Integer entityId;

    private Integer entityType;

    private Integer userId;

    private Integer status;

    private Date createTime;

    @Override
    public String toString() {
        return "UserLike{" +
                "id=" + id +
                ", entityId=" + entityId +
                ", entityType=" + entityType +
                ", userId=" + userId +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }


    public Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }


    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(@Nullable Integer entityId) {
        this.entityId = entityId;
    }


    public Integer getEntityType() {
        return entityType;
    }

    public void setEntityType(@Nullable Integer entityType) {
        this.entityType = entityType;
    }


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(@Nullable Integer userId) {
        this.userId = userId;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(@Nullable Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
