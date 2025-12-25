package org.microserviceteam.social.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_follow")
public class FollowDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long followerId;
    private Long followingId;
    private String instanceId;
    private LocalDateTime createTime;
}
