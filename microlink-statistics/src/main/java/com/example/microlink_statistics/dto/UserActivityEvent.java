package com.example.microlink_statistics.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户活动事件数据传输对象 (DTO)。
 * 用于承载从 Kafka 接收的用户相关活动消息，例如用户登录、浏览页面等。
 *
 * @author Rolland1944
 */
@Data
public class UserActivityEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 触发事件的用户 ID。
     */
    private String userId;

    /**
     * 事件类型。
     * 建议使用枚举或常量来定义，例如 "USER_LOGIN", "PAGE_VIEW"。
     */
    private String eventType;

    /**
     * 事件发生的时间戳。
     */
    private LocalDateTime timestamp;

    // 可以根据需要添加其他字段，例如：IP 地址、设备信息等。
}
