package org.microserviceteam.workflow.delegate.push;

import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.workflow.client.PushClient;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("pushDelegate")
public class PushDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PushDelegate.class);

    @Autowired
    private PushClient pushClient;

    /**
     * 业务逻辑实现在 run() 方法中，由 BaseWorkflowDelegate 统一调度并记录日志
     */
    @Override
    protected String run(DelegateExecution execution) throws Exception {
        // 1. 从流程变量中提取主流程传过来的数据
        // 通常由消息启动事件 (MessageStartEvent) 传入
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");

        logger.info("========================================");
        logger.info(">>> [推送服务Delegate] 监听到消息并执行远程推送调用");
        logger.info(">>> 目标用户: {}", userId);
        logger.info(">>> 触发动作: {}", action);

        // 2. 构造推送文案
        String pushMessage = String.format("你好 %s，系统已处理完您的 %s 操作，感谢您的参与！", userId, action);

        String output;
        try {
            // 3. 执行远程 Feign 调用调用 message-push-service
            logger.info(">>> [Feign调用] 正在请求推送微服务接口...");
            pushClient.sendNotification(userId, pushMessage);

            output = "SUCCESS: Notification sent via Feign to " + userId;
            logger.info(">>> {}", output);
        } catch (Exception e) {
            // 4. 异常处理：记录失败信息，但不一定抛出异常（取决于流程是否允许失败）
            output = "FAILED: Push notification error: " + e.getMessage();
            logger.error(">>> [推送子流程异常] 远程服务调用失败: ", e);
            // 如果业务要求推送失败必须回滚流程，可在此处 throw e;
        }

        logger.info("========================================");

        // 返回的结果会被 BaseWorkflowDelegate 存入 Constants.LAST_OUTPUT 变量中
        return output;
    }
}