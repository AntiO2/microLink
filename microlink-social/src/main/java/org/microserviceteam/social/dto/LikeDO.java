package org.microserviceteam.social.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

// 1. 点赞实体
@Data
@TableName("t_like")
public class LikeDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Long userId;
    private Integer status; // 1:点赞, 0:取消
    private String instanceId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}