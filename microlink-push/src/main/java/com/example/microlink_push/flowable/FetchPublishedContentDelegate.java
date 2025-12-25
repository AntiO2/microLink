package com.example.microlink_push.flowable;

import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.dto.PaginatedResponse;
import com.example.microlink_push.service.ContentServiceClient;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("fetchPublishedContentDelegate")
public class FetchPublishedContentDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FetchPublishedContentDelegate.class);

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Flowable Task: Fetching published content...");
        // Fetch the 50 most recent published articles as candidates
        PaginatedResponse<ContentDTO> response = contentServiceClient.listPublishedContent(0, 50, "PUBLISHED");
        List<ContentDTO> candidateContent = response.getContent();

        // Store the list of candidates in a process variable for the next step
        execution.setVariable("candidateContent", candidateContent);
        logger.info("Found {} candidate content items.", candidateContent.size());
    }
}

