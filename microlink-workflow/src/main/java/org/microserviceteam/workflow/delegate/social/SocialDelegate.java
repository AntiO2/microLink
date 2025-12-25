package org.microserviceteam.workflow.delegate.social;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.workflow.client.SocialClient;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("socialDelegate")
public class SocialDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SocialDelegate.class);

    @Autowired
    SocialClient socialClient;

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String action = ProcessVariableUtil.getString(execution, "action", "");
        String userId = ProcessVariableUtil.getString(execution, "userId", "");
        String content = ProcessVariableUtil.getString(execution, "content", "");

        // 1. 发起校验
        Result<Boolean> checkResult = socialClient.checkCompliance(userId, action, content);

        // 2. 根据结果设置状态变量（不抛出错误）
        if (checkResult.getCode() != ResultCode.SUCCESS.getCode()) {
            // 标记校验未通过
            execution.setVariable("isPassed", false);
            execution.setVariable("errorCode", checkResult.getCode());

            // 遵循规范：更新 lastOutput 为错误原因
            String output = "Check Failed: " + checkResult.getMessage();
            execution.setVariable(Constants.LAST_OUTPUT, output);

            logger.warn(">>> [社交服务] 校验未通过，流程继续. 原因: {}", output);
            return output;
        } else {
            // 标记校验通过
            execution.setVariable("isPassed", true);

            // 遵循规范：更新 lastOutput 为成功摘要
            String output = "Check Passed: " + action;
            execution.setVariable(Constants.LAST_OUTPUT, output);

            logger.info(">>> [社交服务] 校验通过.");
            return output;
        }
    }
}