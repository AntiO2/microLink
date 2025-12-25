package org.microserviceteam.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.social.dto.CommentDO;
import org.microserviceteam.social.dto.FollowDO;
import org.microserviceteam.social.dto.LikeDO;
import org.microserviceteam.social.entity.SocialRecord;
import org.microserviceteam.social.mapper.CommentMapper;
import org.microserviceteam.social.mapper.FollowMapper;
import org.microserviceteam.social.mapper.LikeMapper;
import org.microserviceteam.social.mapper.SocialMapper;
import org.microserviceteam.social.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SocialServiceImpl implements SocialService {

    @Autowired
    private LikeMapper likeMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FollowMapper followMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecord(String userId, String action, String instanceId, String content) {
        Long uId = Long.parseLong(userId);

        switch (action.toUpperCase()) {
            case "LIKE":
                LikeDO like = new LikeDO();
                like.setUserId(uId);
                like.setContentId(Long.parseLong(content)); // 点赞时content即为目标ID
                like.setInstanceId(instanceId);
                likeMapper.insert(like);
                break;
            case "COMMENT":
                // 约定格式 content = "targetId:text"
                String[] parts = content.split(":", 2);

                CommentDO comment = new CommentDO();
                comment.setUserId(uId);
                comment.setContentId(Long.parseLong(parts[0]));
                comment.setCommentText(parts.length > 1 ? parts[1] : "");
                comment.setInstanceId(instanceId);
                commentMapper.insert(comment);
                break;
            case "FOLLOW":
                FollowDO follow = new FollowDO();
                follow.setFollowerId(uId);
                follow.setFollowingId(Long.parseLong(content));
                follow.setInstanceId(instanceId);
                try {
                    followMapper.insert(follow);
                } catch (Exception e)
                {
                    log.error("重复关注");
                }

                break;
        }
        log.info(">>> 业务数据已持久化. 动作: {}, 实例ID: {}", action, instanceId);
    }

    @Override
    public Result<Boolean> checkCompliance(String userId, String action, String content) {
        try {
            Long uId = Long.parseLong(userId);
            if ("LIKE".equalsIgnoreCase(action)) {
                Long targetId = Long.parseLong(content);
                long count = likeMapper.selectCount(new LambdaQueryWrapper<LikeDO>()
                        .eq(LikeDO::getUserId, uId).eq(LikeDO::getContentId, targetId).eq(LikeDO::getStatus, 1));
                if (count > 0) return Result.error(ResultCode.DUPLICATE_LIKE, "请勿重复点赞");
            }
            if ("COMMENT".equalsIgnoreCase(action) && content.contains("恶意")) {
                return Result.error(ResultCode.SENSITIVE_CONTENT, "内容含有敏感词");
            }
            if ("FOLLOW".equalsIgnoreCase(action)) {
                Long followingId = Long.parseLong(content); // 关注时 content 存的是被关注者ID

                // 检查是否关注了自己（业务基本逻辑）
                if (uId.equals(followingId)) {
                    return Result.error(ResultCode.VALIDATION_FAILED, "您不能关注自己");
                }
                Long count = followMapper.selectCount(new LambdaQueryWrapper<FollowDO>()
                        .eq(FollowDO::getFollowerId, uId)
                        .eq(FollowDO::getFollowingId, followingId));

                if (count > 0) {
                    return Result.error(ResultCode.VALIDATION_FAILED, "您已关注该用户，请勿重复关注");
                }
            }
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}