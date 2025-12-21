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

    @Column(nullable = false)
    private String text;

    private String mediaUrl;

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
}
