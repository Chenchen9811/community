package com.chen.community.service;

import com.chen.community.entity.Message;

import java.util.List;

public interface MessageService {
    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信。
    List<Message> findConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量.
    int findConversationsCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> findLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int findLettersCount(String conversationId);

    // 查询未读私信的数量
    int findLettersUnreadCount(int userId, String conversationId);

    // 增加一条消息
    int addMessage(Message message);

    // 读取消息
    int readMessage(List<Integer> ids);

    // 查询某个主题下最新的通知
    Message findLatestNotice(int userId, String topic);

    // 查询某个主题包含的通知的数量
    int findNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int findUnreadNoticeCount(int userId, String topic);

    // 查询某个主题包含的通知列表
    List<Message> findNotices(int userId, String topic, int offset, int limit);
}
