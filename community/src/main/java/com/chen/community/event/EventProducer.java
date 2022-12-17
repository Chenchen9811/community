package com.chen.community.event;

import com.alibaba.fastjson.JSONObject;
import com.chen.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件以JSON字符串的方式发布到指定的主题，消费者收到后可以转换为原始的Event对象，
        // 至于怎么处理这个Event对象则由消费者来决定， 生产者只负责发送
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
