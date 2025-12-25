package com.example.microlink_push.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * DTO representing an event related to the content lifecycle (e.g., creation, publication).
 * This will be the object received from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentLifecycleEvent implements Serializable {
    /**
     * A unique identifier for the event type, e.g., "CONTENT_PUBLISHED".
     */
    private String eventType;

    /**
     * The ID of the content that this event pertains to.
     */
    private Long contentId;

    /**
     * The ID of the author of the content.
     */
    private String authorId;

    /**
     * The new status of the content, e.g., "PUBLISHED".
     */
    private String status;
}

