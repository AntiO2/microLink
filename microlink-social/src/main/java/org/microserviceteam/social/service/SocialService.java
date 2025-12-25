package org.microserviceteam.social.service;

import org.microserviceteam.common.Result;

public interface SocialService {
    /**
     * 统一保存社交互动记录
     * @param userId 用户ID
     * @param action 动作类型 (LIKE, COMMENT, FOLLOW)
     * @param instanceId 关联的流程实例ID
     */
    void saveRecord(String userId, String action, String instanceId, String content);

    Result<Boolean> checkCompliance(String userId, String action, String content);
}