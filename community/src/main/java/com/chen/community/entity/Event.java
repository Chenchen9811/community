package com.chen.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    // 主题，事件的类型，比如评论、点赞、关注
    private String topic;

    // 事件触发的人
    private int userId;

    // 触发事件的人对什么实体做了操作，因此需要记录可以唯一标识实体的实体类型和实体id
    private int entityType;

    private int entityId;

    // 实体所属人，比如某个帖子属于某个人
    private int entityUserId;

    // 存放事件中的其他信息
    private Map<String, Object> data = new HashMap<>();

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", entityUserId=" + entityUserId +
                ", data=" + data +
                '}';
    }

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
