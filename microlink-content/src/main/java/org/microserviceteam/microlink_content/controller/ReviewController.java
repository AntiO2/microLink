package org.microserviceteam.microlink_content.controller;

import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.service.ContentService;
import org.microserviceteam.microlink_content.service.ProcessService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content/review")
public class ReviewController {
    @Autowired
    private ProcessService processService;

    @Autowired
    private ContentService contentService;

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getReviewTasks() {
        // In real world, we check if user is admin
        List<Task> tasks = processService.getTasksByProcessKey("admin", "content-review");
        
        List<Map<String, Object>> response = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("taskName", task.getName());
            
            try {
                // Fetch Content ID associated with this task
                Object contentId = processService.getVariable(task.getProcessInstanceId(), "contentId");
                item.put("contentId", contentId);
            } catch (Exception e) {
                // Ignore if variable missing
            }
            
            response.add(item);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}")
    public ResponseEntity<?> completeReview(@PathVariable String taskId, @RequestBody Map<String, Boolean> body) {
        boolean approved = body.getOrDefault("approved", false);
        
        try {
            Task task = processService.getTask(taskId);
            if (task != null) {
                Object contentIdObj = processService.getVariable(task.getProcessInstanceId(), "contentId");
                if (contentIdObj != null) {
                    Long contentId = Long.valueOf(contentIdObj.toString());
                    if (approved) {
                        contentService.updateStatus(contentId, Content.ContentStatus.PUBLISHED);
                    } else {
                        contentService.updateStatus(contentId, Content.ContentStatus.REJECTED);
                    }
                }
            }
        } catch (Exception e) {
            // Log error but proceed to complete task
        }
        
        processService.completeTask(taskId);
        return ResponseEntity.ok(Map.of("message", "Review completed"));
    }
}
