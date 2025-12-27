package org.microserviceteam.microlink_content.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WorkflowClient {

    private final RestTemplate restTemplate;

    @Value("${microlink.workflow-service.url:http://localhost:8083}")
    private String workflowServiceUrl;

    public WorkflowClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void startProcess(String processKey, Map<String, Object> variables) {
        try {
            restTemplate.postForObject(
                workflowServiceUrl + "/workflow/start/" + processKey,
                variables,
                Object.class
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error (log it, etc.)
        }
    }
}
