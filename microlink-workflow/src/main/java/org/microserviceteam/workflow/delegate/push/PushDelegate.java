package org.microserviceteam.workflow.delegate.push;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("pushDelegate")
public class PushDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PushDelegate.class);

    /**
     * 注意：如果你的 BaseWorkflowDelegate 重写了 execute 并调用了 run()，
     * 那么具体的业务逻辑应该实现在 run() 方法中。
     */
    @Override
    protected String run(DelegateExecution execution) throws Exception {
        // 1. 从流程变量中提取主流程传过来的数据
        // RECORD_STATS_MSG 和 SEND_PUSH_MSG 启动时带入的变量
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");

        logger.info("========================================");
        logger.info(">>> [推送服务] 监听到消息并开启推送子流程");
        logger.info(">>> 关联用户: {}", userId);
        logger.info(">>> 触发动作: {}", action);

        // 2. 执行 Mock 推送逻辑
        String resultMessage = mockPushServiceCall(userId, action);

        logger.info(">>> [推送服务] 任务处理完成.");
        logger.info("========================================");

        // 返回的结果通常会被 BaseWorkflowDelegate 记录到流程变量中
        return resultMessage;
    }

    private String mockPushServiceCall(String userId, String action) {
        String mockContent = String.format("你好 %s，你的 %s 操作已经处理成功啦！", userId, action);

        logger.info(">>> 正在调取第三方推送接口 (AppPush/Email/SMS)...");
        logger.info(">>> 推送正文: \"{}\"", mockContent);

        // 模拟成功响应
        return "SUCCESS: Push sent to " + userId;
    }
}