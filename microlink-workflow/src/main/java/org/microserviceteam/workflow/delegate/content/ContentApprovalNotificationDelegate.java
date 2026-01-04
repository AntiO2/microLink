package org.microserviceteam.workflow.delegate.content;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("contentApprovalNotificationDelegate")
public class ContentApprovalNotificationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object contentId = execution.getVariable("contentId");
        Object approved = execution.getVariable("approved");
        Object autoCheckPassed = execution.getVariable("autoCheckPassed");
        
        System.out.println("Sending content notification for content " + contentId 
            + ". Auto check passed: " + autoCheckPassed 
            + ". Admin approved: " + approved);
    }
}
