package org.microserviceteam.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.ContentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContentPublishDelegate implements JavaDelegate {

    @Autowired
    private ContentClient contentClient;

    @Override
    public void execute(DelegateExecution execution) {
        Long contentId = (Long) execution.getVariable("contentId");
        if (contentId == null) {
            // Handle error or try to parse from string if passed as string
            Object contentIdObj = execution.getVariable("contentId");
            if (contentIdObj != null) {
                contentId = Long.valueOf(contentIdObj.toString());
            }
        }

        if (contentId != null) {
            contentClient.updateStatus(contentId, "PUBLISHED");
        } else {
            throw new RuntimeException("Content ID not found in process variables");
        }
    }
}
