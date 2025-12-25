package org.microserviceteam.social.entity;

import lombok.Data;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("social_record")
public class SocialRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String action;      // LIKE, COMMENT, FOLLOW
    private String targetId;    // 点赞的对象ID或评论的目标ID
    private String content;     // 评论内容
    private String instanceId;  // 关联的工作流实例ID
    private LocalDateTime createTime;
}