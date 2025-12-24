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
        // --- 数据校验 ---
        // 这是保护我们系统内部逻辑的第一道防线。
        // 我们选择记录警告并丢弃消息，而不是抛出异常。
        // 因为抛出异常可能导致 Kafka 消费者不断重试处理这条 "毒丸消息" (Poison Pill Message)。
        if (event == null) {
            logger.warn("Received a null event object. Discarding message.");
            return;
        }
        if (event.getContentId() == null) {
            logger.warn("Received event with null contentId. Discarding message: {}", event);
            return;
        }
        // 使用 Spring Framework 的 StringUtils.hasText 来判断字符串是否为 null 或空字符串
        if (!StringUtils.hasText(event.getEventType())) {
            logger.warn("Received event with empty or null eventType. Discarding message: {}", event);
            return;
        }
        // --- 校验结束 ---
        try {
            // 将事件委托给 service 层进行处理
            statisticsService.updateContentInteraction(event);
            logger.debug("Successfully processed content interaction for contentId: {}", event.getContentId());
        } catch (Exception e) {
            // 捕获 service 层可能抛出的任何未预料异常，记录错误日志，防止消费者线程崩溃。
            logger.error("Error processing content interaction event: {}", event, e);
        }
    }
}
