package org.microserviceteam.workflow.controller;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.microserviceteam.common.Result;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/workflow/dynamic")
public class DynamicFlowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/deploy")
    public Map<String, String> deploy(@RequestBody String bpmnXml, @RequestParam String flowName) {
        return workflowService.deploy(bpmnXml, flowName);
    }

    // 2. 仅运行
    @PostMapping("/start")
    public Result<Map<String, Object>> start(@RequestParam String processKey, @RequestBody Map<String, Object> variables) {
        Map<String, Object> deepProcessTree = workflowService.start(processKey, variables);
        return Result.success(deepProcessTree);
    }
}