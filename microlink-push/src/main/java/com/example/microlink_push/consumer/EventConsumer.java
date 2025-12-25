package com.example.microlink_push.consumer;

import com.example.microlink_push.dto.ContentLifecycleEvent;
import com.example.microlink_push.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
/**
 * Placeholder for message queue consumers.
 * This class would listen to events from other services (e.g., a new content
 * being published) to perform asynchronous updates, such as updating a cache
 * of hot content.
 */
@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private final PushService pushService;
    // Dependency injection via constructor, which is a best practice.
    @Autowired
    public EventConsumer(PushService pushService) {
        this.pushService = pushService;
    }
    /**
     * Listens to the "content-lifecycle-topic" for messages.
     *
     * @param event The ContentLifecycleEvent object received from Kafka.
     */
    @KafkaListener(topics = "content-lifecycle-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void handleContentLifecycleEvent(ContentLifecycleEvent event) {
        logger.info("Received content lifecycle event: {}", event);
        // --- Data Validation ---
        // This is a critical defense to prevent "Poison Pill Messages" from
        // crashing the consumer, which would lead to endless retries.
        if (event == null) {
            logger.warn("Received a null event object. Discarding message.");
            return;
        }
        if (event.getContentId() == null) {
            logger.warn("Received event with null contentId. Discarding message: {}", event);
            return;
        }
        if (!StringUtils.hasText(event.getEventType())) {
            logger.warn("Received event with empty or null eventType. Discarding message: {}", event);
            return;
        }
        // --- End of Validation ---
        // We only care about events where content has been successfully published.
        if ("CONTENT_PUBLISHED".equalsIgnoreCase(event.getEventType())) {
            try {
                // Delegate the actual business logic to the service layer.
                pushService.processNewPublishedContent(event.getContentId());
                logger.debug("Successfully processed 'CONTENT_PUBLISHED' event for contentId: {}", event.getContentId());
            } catch (Exception e) {
                // Catch any unexpected exceptions from the service layer to prevent the consumer thread from dying.
                logger.error("Error processing content lifecycle event: {}", event, e);
            }
        } else {
            logger.debug("Ignoring event of type '{}' as it is not 'CONTENT_PUBLISHED'.", event.getEventType());
        }
    }

}
