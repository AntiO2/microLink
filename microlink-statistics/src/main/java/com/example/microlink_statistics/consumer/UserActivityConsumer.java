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
        // --- 数据校验 ---
        // 同样，这是为了防止无效数据进入我们的核心业务逻辑。
        // 对于 DAU 统计，最重要的字段就是 userId。
        if (event == null) {
            logger.warn("Received a null event object. Discarding message.");
            return;
        }
        if (event.getUserId() == null) {
            logger.warn("Received event with null userId. Discarding message: {}", event);
            return;
        }
        // --- 校验结束 ---
        try {
            // 将事件委托给 service 层进行处理
            statisticsService.recordUserActivity(event);
            logger.debug("Successfully processed user activity for userId: {}", event.getUserId());
        } catch (Exception e) {
            // 捕获 service 层可能发生的任何异常（例如 Redis 连接问题），
            // 记录错误并继续处理下一条消息，避免消费者线程死亡。
            logger.error("Error processing user activity event: {}", event, e);
        }
    }
}
