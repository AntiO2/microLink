package org.microserviceteam.workflow.delegate.social;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.common.Result;
import org.microserviceteam.workflow.client.PushClient;
import org.microserviceteam.workflow.client.SocialClient;
import org.microserviceteam.workflow.client.StatsClient;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.service.WorkflowService;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component("persistenceDelegate")
public class PersistenceDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceDelegate.class);

    @Autowired
    private SocialClient socialClient; // 注入 SocialClient

    @Autowired
    private WorkflowService workflowService;

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        // 1. 获取基础变量
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String content = ProcessVariableUtil.getString(execution, "content", "null");
        String instanceId = execution.getProcessInstanceId();

        // 2. 检查前置校验状态（遵循不抛错、设变量继续规范）
        Boolean isPassed = (Boolean) execution.getVariable("isPassed");
        if (Boolean.FALSE.equals(isPassed)) {
            String skipMsg = "Persistence Skipped: Previous validation failed.";
            logger.warn(">>> [持久化服务] 实例 {} 校验未通过，跳过入库逻辑", instanceId);
            return skipMsg;
        }

        try {
            logger.info(">>> [持久化服务] 准备调用社会化微服务. 实例: {}, 动作: {}", instanceId, action);

            // 3. 执行核心业务入库 (Feign 调用)
            Result<Void> repoResult = socialClient.saveInteractionRecord(userId, action, instanceId, content);

            if (repoResult.getCode() != 200) {
                // 业务入库失败，更新 isPassed 为 false
                execution.setVariable("isPassed", false);
                String errorMsg = "Persistence DB Error: " + repoResult.getMessage();
                execution.setVariable(Constants.LAST_OUTPUT, errorMsg);
                logger.error(">>> [持久化服务] 业务入库失败: {}", errorMsg);
                return errorMsg;
            } else {
                // 4. 入库成功，开始触发协作子流程 (对应原本的 invokeRemoteServices)
                logger.info(">>> [持久化服务] 入库成功，正在通过 WorkflowService 触发子流程...");

                // 准备子流程上下文，注入 parentInstanceId 用于递归回溯
                Map<String, Object> subVars = new HashMap<>(execution.getVariables());
                subVars.put("parentInstanceId", instanceId);

                // 触发统计流程 (Stats)
                // invokeStats(execution, subVars);
            }

        } catch (Exception e) {
            // 系统级异常（超时、宕机等），按规范不抛出异常，记录到变量
            execution.setVariable("isPassed", false);
            String sysError = "Persistence System Exception: " + e.getMessage();
            execution.setVariable(Constants.LAST_OUTPUT, sysError);
            logger.error(">>> [持久化服务] 系统故障: {}", sysError);
        }
        return "Social [持久化服务] 入库成功";
    }

    private void invokeStats(DelegateExecution execution, Map<String, Object> subVars) {
        Map<String, Object> statsInfo = workflowService.start("stats-process", subVars);
        // 触发推送流程 (Push)
        Map<String, Object> pushInfo = workflowService.start("push-process", subVars);

        // 5. 按照规范，从子流程结果中提取 lastOutput 并汇总到当前节点的 lastOutput
        String statsOut = (String) statsInfo.getOrDefault(Constants.LAST_OUTPUT, "N/A");
        String pushOut = (String) pushInfo.getOrDefault(Constants.LAST_OUTPUT, "N/A");

        String finalSummary = String.format("All Done. [Persistence: OK], [Stats: %s], [Push: %s]",
                statsOut, pushOut);

        execution.setVariable(Constants.LAST_OUTPUT, finalSummary);
    }
}