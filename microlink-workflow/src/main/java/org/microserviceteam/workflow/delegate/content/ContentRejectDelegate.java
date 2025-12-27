package org.microserviceteam.workflow.delegate.content;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.ContentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("contentRejectDelegate")
public class ContentRejectDelegate implements JavaDelegate {

    @Autowired
    private ContentClient contentClient;

    @Override
    public void execute(DelegateExecution execution) {
        Object contentIdObj = execution.getVariable("contentId");
        if (contentIdObj == null) {
            System.err.println("Content ID not found in process variables");
            return;
        }

        Long contentId = Long.valueOf(contentIdObj.toString());
        System.out.println("Content rejected: " + contentId);
        
        try {
            contentClient.updateStatus(contentId, "REJECTED");
        } catch (Exception e) {
            System.err.println("Feign call to content service failed for rejection: " + e.getMessage());
        }
    }
}
