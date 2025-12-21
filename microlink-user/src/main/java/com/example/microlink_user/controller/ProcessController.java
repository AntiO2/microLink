package com.example.microlink_user.controller;

import com.example.microlink_user.payload.response.ApiResponse;
import com.example.microlink_user.security.services.UserDetailsImpl;
import com.example.microlink_user.service.ProcessService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/process")
public class ProcessController {
    @Autowired
    private ProcessService processService;

    @PostMapping("/start")
    public ResponseEntity<?> startProcess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicant", username);
        
        // Pass userId as businessKey
        String processId = processService.startProcess("user-onboarding", String.valueOf(userDetails.getId()), variables);
        
        Map<String, String> response = new HashMap<>();
        response.put("processId", processId);
        response.put("message", "Onboarding process started for user: " + username);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@RequestParam String assignee) {
        List<Task> tasks = processService.getTasks(assignee);
        List<Map<String, String>> dtos = tasks.stream().map(task -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("name", task.getName());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Assuming tasks are assigned to username
        List<Task> tasks = processService.getTasks(username);
        
        List<Map<String, String>> dtos = tasks.stream().map(task -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", task.getId());
            map.put("name", task.getName());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable String taskId) {
        processService.completeTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task completed", null));
    }
}
