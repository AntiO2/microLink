package org.microserviceteam.workflow.delegate.stats;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("statsDelegate")
public class StatsDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(StatsDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 1. 获取从主流程传递过来的变量
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String instanceId = execution.getProcessInstanceId();

        logger.info(">>> [统计子流程] 开始执行逻辑. 实例ID: {}, 关联用户: {}", instanceId, userId);

        // 2. Mock 统计接口逻辑
        mockStatsServiceCall(action, userId);

        logger.info(">>> [统计子流程] 逻辑处理完成.");
    }

    private void mockStatsServiceCall(String action, String userId) {
        logger.debug(">>> [MOCK] 准备调用远程统计接口: /api/v1/stats/collect");
        // 模拟网络耗时
        try {
            Thread.sleep(100);
            logger.info(">>> [MOCK] 统计数据已同步成功: [Action: {}, User: {}]", action, userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}