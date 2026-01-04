package org.microserviceteam.workflow.delegate.user;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("userNotificationDelegate")
public class UserNotificationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object userId = execution.getVariable("userId");
        Object approved = execution.getVariable("approved");
        
        System.out.println("Sending notification to user " + userId + ". Registration approved: " + approved);
        // 这里可以集成消息推送或邮件服务
    }
}
