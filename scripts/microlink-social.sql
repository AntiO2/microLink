-- 1. 关注关系表
CREATE TABLE `t_follow` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
                            `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
                            `instance_id` VARCHAR(64) COMMENT '关联流程实例ID',
                            `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE KEY `uk_follow` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 评论表
CREATE TABLE `t_comment` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             `content_id` BIGINT NOT NULL COMMENT '内容ID',
                             `user_id` BIGINT NOT NULL COMMENT '评论者ID',
                             `comment_text` TEXT NOT NULL,
                             `instance_id` VARCHAR(64) COMMENT '关联流程实例ID',
                             `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             INDEX `idx_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 点赞记录表
CREATE TABLE `t_like` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `content_id` BIGINT NOT NULL,
                          `user_id` BIGINT NOT NULL,
                          `status` TINYINT DEFAULT 1 COMMENT '1:点赞, 0:取消点赞',
                          `instance_id` VARCHAR(64) COMMENT '关联流程实例ID',
                          `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY `uk_user_content` (`user_id`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;