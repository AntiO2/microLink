package com.example.microlink_push.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO representing a piece of content. This will be the main object
 * in the API response for the push feed.
 */
@Data
public class ContentDTO implements Serializable {
    private Long id;
    private String title;
    private String text;
    private String summary;
    private String mediaUrl;
    private String coverUrl;
    private String contentType; // Using String to be flexible (POST, ARTICLE, VIDEO)
    private String status;      // Using String (PENDING, PUBLISHED, REJECTED)
    private LocalDateTime createdAt;
    private UserDTO author; // Embed author information
}

