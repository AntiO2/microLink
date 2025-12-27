package org.microserviceteam.microlink_content.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContentRejectDelegate implements JavaDelegate {

    @Autowired
    private ContentService contentService;

    @Override
    public void execute(DelegateExecution execution) {
        Long contentId = (Long) execution.getVariable("contentId");
        contentService.updateStatus(contentId, Content.ContentStatus.REJECTED);
    }
}
