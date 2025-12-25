package org.microserviceteam.social.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class CommentDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Long userId;
    private String commentText;
    private String instanceId;
    private LocalDateTime createTime;
}