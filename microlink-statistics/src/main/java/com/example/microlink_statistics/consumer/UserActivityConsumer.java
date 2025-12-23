package com.example.microlink_statistics.consumer;

import com.example.microlink_statistics.dto.UserActivityEvent;
import com.example.microlink_statistics.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消费用户活动相关的 Kafka 消息。
 *
 * @author Rolland1944
 */
@Component
public class UserActivityConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityConsumer.class);

    private final StatisticsService statisticsService;

    @Autowired
    public UserActivityConsumer(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 监听 "user-activity-topic" 主题的消息。
     *
     * @param event 从 Kafka 接收到的用户活动事件对象。
     */
    @KafkaListener(topics = "user-activity-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserActivityEvent(UserActivityEvent event) {
        logger.info("Received user activity event: {}", event);

        // TODO: 在此处添加对 event 的基本校验，例如检查 userId 是否为空。

        // 将事件委托给 service 层进行处理
        statisticsService.recordUserActivity(event);
    }
}
