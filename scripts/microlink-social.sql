-- 1. 关注关系表 (用户关注与粉丝)
CREATE TABLE `t_follow` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
                            `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
                            `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE KEY `uk_follow` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 评论表
CREATE TABLE `t_comment` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             `content_id` BIGINT NOT NULL COMMENT '内容ID (指向内容微服务)',
                             `user_id` BIGINT NOT NULL COMMENT '评论者ID',
                             `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID (用于楼中楼)',
                             `comment_text` TEXT NOT NULL,
                             `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             INDEX `idx_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 点赞记录表 (若追求极致性能，此表可作为Redis持久化后的备份)
CREATE TABLE `t_like` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `content_id` BIGINT NOT NULL,
                          `user_id` BIGINT NOT NULL,
                          `status` TINYINT DEFAULT 1 COMMENT '1:点赞, 0:取消点赞',
                          `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY `uk_user_content` (`user_id`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;