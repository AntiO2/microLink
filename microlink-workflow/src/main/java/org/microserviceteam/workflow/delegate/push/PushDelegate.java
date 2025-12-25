package org.microserviceteam.workflow.delegate.push;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("pushDelegate")
public class PushDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PushDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");

        logger.info(">>> [推送子流程] 捕获到推送任务. 目标用户: {}", userId);

        // 2. Mock 推送接口逻辑
        mockPushServiceCall(userId, action);

        logger.info(">>> [推送子流程] 推送任务结束.");
    }

    private void mockPushServiceCall(String userId, String action) {
        logger.debug(">>> [MOCK] 正在组装推送报文，匹配动作: {}", action);

        String mockContent = String.format("你好 %s，你的 %s 记录已更新。", userId, action);

        // 模拟接口调用
        logger.info(">>> [MOCK] 远程推送请求已发出: { 内容: \"{}\" }", mockContent);
        logger.info(">>> [MOCK] 接收端已确认接收 (Status: 202 ACCEPTED)");
    }
}