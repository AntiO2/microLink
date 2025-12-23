package org.microserviceteam.workflow.service;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkflowService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;

    // 逻辑抽象：部署
    public Map<String, String> deploy(String bpmnXml, String flowName) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        InputStreamSource isr = new InputStreamSource(
                new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))
        );
        BpmnModel bpmnModel = converter.convertToBpmnModel(isr, true, false);

        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("dynamic_" + System.currentTimeMillis() + ".bpmn20.xml", bpmnModel)
                .name(flowName)
                .deploy();

        String processKey = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult()
                .getKey();

        Map<String, String> result = new HashMap<>();
        result.put("deploymentId", deployment.getId());
        result.put("processKey", processKey);
        return result;
    }

    // 逻辑抽象：启动并获取结果
    public String start(String processKey, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, variables);

        // 优先获取你定义的 lastOutput，如果没有则获取 initialInput
        Object finalResult = runtimeService.getVariable(instance.getId(), "lastOutput");
        if (finalResult == null) {
            finalResult = runtimeService.getVariable(instance.getId(), "initialInput");
        }

        return String.format("流程[%s]运行成功! 最终结果: %s", processKey, finalResult);
    }
}
