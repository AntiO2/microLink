package org.microserviceteam.workflow.delegate.content;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.ContentClient;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("contentPublishDelegate")
public class ContentPublishDelegate implements JavaDelegate {

    @Autowired
    private ContentClient contentClient;

    @Autowired
    private WorkflowService workflowService;

    @Override
    public void execute(DelegateExecution execution) {
        Object contentIdObj = execution.getVariable("contentId");
        if (contentIdObj == null) {
            throw new RuntimeException("Content ID not found in process variables");
        }
        
        Long contentId = Long.valueOf(contentIdObj.toString());

        try {
            contentClient.updateStatus(contentId, "PUBLISHED");
        } catch (Exception e) {
            System.err.println("Feign call to content service failed: " + e.getMessage());
        }

        // 触发后续流程 (V2 版本)
        Map<String, Object> vars = new HashMap<>();
        vars.put("contentId", contentId);
        vars.put("action", "CONTENT_PUBLISH");

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
