package org.microserviceteam.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ContentReviewDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Long contentId = (Long) execution.getVariable("contentId");
        System.out.println("Executing automated content review for Content ID: " + contentId);
        
        // Simulating some logic (e.g. text analysis API call)
        // For now, we assume it passes automated checks and goes to manual review or publish
        execution.setVariable("autoCheckPassed", true);
    }
}
