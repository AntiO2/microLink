package com.example.microlink_content.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@Data
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false)
    private String text;

    private String mediaUrl;
    
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(nullable = false)
    private String authorId;

    @Enumerated(EnumType.STRING)
    private ContentStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ContentStatus {
        PENDING,
        PUBLISHED,
        REJECTED
    }

    public enum ContentType {
        POST,
        ARTICLE,
        VIDEO
    }
}
