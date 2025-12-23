package org.microserviceteam.workflow.controller;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
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

    // 1. 仅部署
    @PostMapping("/deploy")
    public Map<String, String> deploy(@RequestBody String bpmnXml, @RequestParam String flowName) {
        return workflowService.deploy(bpmnXml, flowName);
    }

    // 2. 仅运行
    @PostMapping("/start")
    public String start(@RequestParam String processKey, @RequestBody Map<String, Object> variables) {
        return workflowService.start(processKey, variables);
    }

    // 3. 二合一接口：部署并立即运行
    @PostMapping("/deploy-and-start")
    public String deployAndStart(@RequestBody Map<String, Object> requestBody,
                                 @RequestParam String flowName) {
        // 从 JSON 中提取 XML 和 变量
        String bpmnXml = (String) requestBody.get("bpmnXml");
        Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");

        // 调用 Service 组合逻辑
        Map<String, String> deployInfo = workflowService.deploy(bpmnXml, flowName);
        return workflowService.start(deployInfo.get("processKey"), variables);
    }
}