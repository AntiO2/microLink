package com.example.microlink_statistics.consumer;

import com.example.microlink_statistics.dto.ContentInteractionEvent;
import com.example.microlink_statistics.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消费内容互动相关的 Kafka 消息。
 *
 * @author Rolland1944
 */
@Component
public class ContentInteractionConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ContentInteractionConsumer.class);

    private final StatisticsService statisticsService;

    @Autowired
    public ContentInteractionConsumer(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 监听 "content-interaction-topic" 主题的消息。
     *
     * @param event 从 Kafka 接收到的内容互动事件对象。
     */
    @KafkaListener(topics = "content-interaction-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void handleContentInteractionEvent(ContentInteractionEvent event) {
        logger.info("Received content interaction event: {}", event);

        // TODO: 在此处添加对 event 的基本校验，例如检查 contentId 和 eventType 是否为空。

        // 将事件委托给 service 层进行处理
        statisticsService.updateContentInteraction(event);
    }
}
