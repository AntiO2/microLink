package org.microserviceteam.workflow.delegate.content;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.ContentClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("contentReviewDelegate")
public class ContentReviewDelegate implements JavaDelegate {

    @Autowired
    private ContentClient contentClient;

    @Override
    public void execute(DelegateExecution execution) {
        Object contentIdObj = execution.getVariable("contentId");
        if (contentIdObj == null) {
            System.err.println("Content ID not found in process variables");
            execution.setVariable("autoCheckPassed", false);
            return;
        }
        
        Long contentId = Long.valueOf(contentIdObj.toString());

        System.out.println("Executing automated content review for Content ID: " + contentId);
        
        boolean passed = false;
        try {
            Boolean result = contentClient.checkContent(contentId).getBody();
            passed = result != null && result;
        } catch (Exception e) {
            System.err.println("Error calling content check: " + e.getMessage());
            // 如果连不上服务，为了演示流程跑通，我们可以根据变量来模拟结果
            Object manualPassed = execution.getVariable("autoCheckPassed");
            if (manualPassed != null) {
                passed = Boolean.parseBoolean(manualPassed.toString());
            }
        }
        
        execution.setVariable("autoCheckPassed", passed);
    }
}
