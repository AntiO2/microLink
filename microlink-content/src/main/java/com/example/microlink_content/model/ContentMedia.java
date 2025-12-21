package com.example.microlink_content.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_media")
@Data
public class ContentMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    private MediaType fileType;

    private Long contentId;

    @Column(nullable = false)
    private String uploaderId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MediaType {
        IMAGE,
        VIDEO,
        OTHER
    }
}
