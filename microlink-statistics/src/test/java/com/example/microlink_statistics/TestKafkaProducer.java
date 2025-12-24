package com.example.microlink_statistics.test;

import com.example.microlink_statistics.dto.ContentInteractionEvent;
import com.example.microlink_statistics.dto.UserActivityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 这是一个用于测试的组件，它会在 Spring Boot 应用启动后自动运行。
 * 它会向 Kafka 发送一系列模拟事件，以便我们测试整个数据处理流程。
 * 在生产环境中，应该移除或禁用此类。
 *
 * @author CAN (Code Anything Now)
 */
@Component
public class TestKafkaProducer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestKafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("==========================================================");
        logger.info("===            STARTING TEST DATA INJECTION            ===");
        logger.info("==========================================================");

        // --- 模拟用户活跃事件 (用于计算 DAU) ---
        // 模拟 3 个不同用户的活动
        sendUserActivity("user-101");
        sendUserActivity("user-256");
        sendUserActivity("user-999");
        // 模拟 user-101 的重复活动，HyperLogLog 会自动去重
        sendUserActivity("user-101");

        // --- 模拟内容交互事件 ---
        // 针对内容 ID 1001
        sendContentInteraction(1001L, "VIEW");
        sendContentInteraction(1001L, "VIEW");
        sendContentInteraction(1001L, "LIKE");

        // 针对内容 ID 2002
        sendContentInteraction(2002L, "VIEW");
        sendContentInteraction(2002L, "SHARE");
        sendContentInteraction(2002L, "COMMENT");
        sendContentInteraction(2002L, "LIKE");

        logger.info("==========================================================");
        logger.info("===            TEST DATA INJECTION COMPLETE            ===");
        logger.info("==========================================================");
    }

    private void sendUserActivity(String userId) {
        UserActivityEvent event = new UserActivityEvent();
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setActivityType("APP_OPEN");
        logger.info("Sending UserActivityEvent for userId: {}", userId);
        kafkaTemplate.send("user-activity", event);
    }

    private void sendContentInteraction(Long contentId, String interactionType) {
        ContentInteractionEvent event = new ContentInteractionEvent();
        event.setContentId(contentId);
        event.setUserId("user-test-" + contentId); // 简单模拟一个用户ID
        event.setInteractionType(interactionType);
        event.setTimestamp(LocalDateTime.now());
        logger.info("Sending ContentInteractionEvent for contentId: {}, type: {}", contentId, interactionType);
        kafkaTemplate.send("content-interactions", event);
    }
}
