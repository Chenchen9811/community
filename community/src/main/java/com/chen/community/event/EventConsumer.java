package com.chen.community.event;

import com.alibaba.fastjson.JSONObject;
import com.chen.community.dao.DiscussPostMapper;
import com.chen.community.entity.DiscussPost;
import com.chen.community.entity.Event;
import com.chen.community.entity.Message;
import com.chen.community.service.ElasticsearchService;
import com.chen.community.service.MessageService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.RedisKeyUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    // 给某个人发消息最终就是将事件转为一条消息然后插入到Message数据库中。
    @Autowired
    private MessageService messageService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleEventMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 将JSON字符串恢复为Event
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误！");
            return;
        }

        // 发通知时是由系统来发的，因此这里假设系统是一个用户并且id为1，那么Message表中的from_id永远为1，
        // 因此conversation_id再把from_id和to_id拼接起来就没有意义了，于是conversation_id存放的就是事件主题，比如是评论、点赞还是关注
        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID); // 系统用户假设为1。
        message.setToId(event.getEntityUserId()); // 设置消息发给谁
        message.setConversationId(event.getTopic());// 设置主题
        message.setCreateTime(new Date()); // 设置事件
        // 设置内容，其中要包含前端页面中拼出如下语句内容：
        // 用户xxx评论了你的帖子...
        // 用户xxx点赞了你的帖子...
        // 用户xxx关注了你...
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId()); // 触发事件的userId
        content.put("entityType", event.getEntityType());// 实体类型
        content.put("entityId", event.getEntityId()); // 实体ID
        // 存放事件中带来的其他内容
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        // 将消息存入Message表中
        messageService.addMessage(message);
    }


    // 消费发帖事件（处理新增帖子、给帖子评论时的事件）
    @KafkaListener(topics = {TOPIC_PUBLIC})
    public void handlePublicMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 将JSON字符串恢复为Event
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误！");
            return;
        }

        // 从数据库中搜索帖子
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(event.getEntityId());
        // 存入ES服务器中
        elasticsearchService.saveDiscussPost(discussPost);
    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 将JSON字符串恢复为Event
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误！");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {TOPIC_DELETE_CACHE})
    public void handleDeleteCache(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        // 将JSON字符串恢复为Event
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息的格式错误！");
            return;
        }
        checkIsDeleted(event.getEntityType(), event.getEntityId());
    }

    private void checkIsDeleted(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if (!redisTemplate.hasKey(entityLikeKey)) {
            Event event = new Event()
                    .setTopic(TOPIC_DELETE_CACHE)
                    .setEntityId(entityId)
                    .setEntityType(entityType);
            eventProducer.fireEvent(event);
        }
    }




}
