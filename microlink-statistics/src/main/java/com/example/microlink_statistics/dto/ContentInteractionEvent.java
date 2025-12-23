package com.example.microlink_statistics.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容互动事件数据传输对象 (DTO)。
 * <p>
 * 用于承载从 Kafka 接收的内容相关互动消息，例如点赞、评论、分享等。
 * </p>
 *
 * @author Rolland
 */
@Data
public class ContentInteractionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 触发事件的用户 ID。
     */
    private Long userId;

    /**
     * 被互动的内容 ID。
     */
    private Long contentId;

    /**
     * 互动类型。
     * 建议使用枚举或常量来定义，例如 "LIKE", "UNLIKE", "COMMENT", "SHARE"。
     */
    private String eventType;

    /**
     * 事件发生的时间戳。
     */
    private LocalDateTime timestamp;

    // 可以根据需要添加其他字段，例如：评论内容ID (如果是评论事件)。
}
