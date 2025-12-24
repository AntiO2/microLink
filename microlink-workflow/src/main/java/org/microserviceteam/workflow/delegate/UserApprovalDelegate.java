package org.microserviceteam.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserApprovalDelegate implements JavaDelegate {

    @Autowired
    private UserClient userClient;

    @Override
    public void execute(DelegateExecution execution) {
        Long userId = (Long) execution.getVariable("userId");
        if (userId == null) {
            Object userIdObj = execution.getVariable("userId");
            if (userIdObj != null) {
                userId = Long.valueOf(userIdObj.toString());
            }
        }

        if (userId != null) {
            userClient.updateStatus(userId, "ACTIVE");
        } else {
            throw new RuntimeException("User ID not found in process variables");
        }
    }
}
