package com.example.microlink_statistics.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 内容热度统计实体类。
 * 每一条记录对应一个内容的热度数据。
 *
 * @author Rolland1944
 */
@Entity
@Table(name = "content_stats")
@Data
public class ContentStats {

    /**
     * 使用内容 ID 作为主键。
     */
    @Id
    private Long contentId;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long likeCount = 0L;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long commentCount = 0L;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long shareCount = 0L;
}
