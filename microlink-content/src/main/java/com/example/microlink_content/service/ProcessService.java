package com.example.microlink_content.service;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProcessService {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Transactional
    public String startProcess(String processKey, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, variables);
        return instance.getId();
    }
    
    @Transactional
    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    @Transactional
    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    @Transactional
    public Object getVariable(String processInstanceId, String variableName) {
        return runtimeService.getVariable(processInstanceId, variableName);
    }

    @Transactional
    public Task getTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }
}
