package org.microserviceteam.workflow.delegate.user;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.UserClient;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("userApprovalDelegate")
public class UserApprovalDelegate implements JavaDelegate {

    @Autowired
    private UserClient userClient;

    @Autowired
    private WorkflowService workflowService;

    @Override
    public void execute(DelegateExecution execution) {
        Object userIdObj = execution.getVariable("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in process variables");
        }
        
        Long userId = Long.valueOf(userIdObj.toString());

        try {
            userClient.updateStatus(userId, "ACTIVE");
        } catch (Exception e) {
            System.err.println("Feign call to user service failed: " + e.getMessage());
        }

        // 触发后续流程 (V2 版本)
        Map<String, Object> vars = new HashMap<>();
        vars.put("userId", userId);
        vars.put("action", "USER_ONBOARDING");

        try {
            workflowService.startByMessage("INDEX_SYNC_MESSAGE", vars, execution);
        } catch (Exception e) {
            System.err.println("Failed to trigger INDEX_SYNC_MESSAGE: " + e.getMessage());
        }
        try {
            workflowService.startByMessage("SEND_PUSH_MSG", vars, execution);
        } catch (Exception e) {
            System.err.println("Failed to trigger SEND_PUSH_MSG: " + e.getMessage());
        }
    }
}
