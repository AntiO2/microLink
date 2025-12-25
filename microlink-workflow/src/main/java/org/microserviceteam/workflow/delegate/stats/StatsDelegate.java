package org.microserviceteam.workflow.delegate.stats;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("statsDelegate")
public class StatsDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(StatsDelegate.class);

    /**
     * 实现父类的 run 方法，业务逻辑的核心落脚点
     */
    @Override
    protected String run(DelegateExecution execution) throws Exception {
        // 1. 获取从主流程通过消息传递过来的变量
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String instanceId = execution.getProcessInstanceId();

        logger.info("========================================");
        logger.info(">>> [统计服务] 接收到统计指令. 实例ID: {}", instanceId);
        logger.info(">>> 待处理动作: {}, 目标用户: {}", action, userId);

        // 2. 执行 Mock 统计处理逻辑
        String result = mockStatsServiceCall(action, userId);

        logger.info(">>> [统计服务] 统计子流程处理结束.");
        logger.info("========================================");

        // 返回的结果会被父类处理，通常会存入变量 statsDelegate_result 中
        return result;
    }

    private String mockStatsServiceCall(String action, String userId) {
        logger.info(">>> [Feign] 正在调取分析系统接口: /api/v1/stats/collect");

        // 模拟业务处理耗时
        try {
            Thread.sleep(200);
            logger.info(">>> 数据分析完成: 用户 {} 完成了一次 {} 操作，权重已更新。", userId, action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR: Thread Interrupted";
        }

        return "SUCCESS: Action [" + action + "] for User [" + userId + "] recorded.";
    }
}