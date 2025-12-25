package org.microserviceteam.workflow.service;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.microserviceteam.workflow.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


@Service
public class WorkflowService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

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

    /**
     * 核心启动与全量结果采集方法
     */
    public Map<String, Object> start(String processKey, Map<String, Object> variables) {
        // 1. 启动流程
        variables.put("executionLogs", new ArrayList<String>());
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey, variables);
        String instanceId = pi.getId();

        // 2. 采集并组装该实例的详尽深度信息
        return collectDeepInfo(instanceId);
    }

    public Map<String, Object> collectDeepInfo(String instanceId) {
        Map<String, Object> node = new HashMap<>();

        // 1. 获取当前实例的历史记录
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null) return node;

        // 2. 采集当前节点的变量快照 (拿到 lastOutput)
        Map<String, Object> vars = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instanceId).list()
                .stream().collect(Collectors.toMap(
                        HistoricVariableInstance::getVariableName,
                        v -> v.getValue() == null ? "null" : v.getValue()
                ));

        // 3. 采集当前节点的轨迹 (Execution Path)
        List<Map<String, Object>> path = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list()
                .stream().map(activity -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("node", activity.getActivityName());
                    m.put("duration", activity.getDurationInMillis() + "ms");
                    return m;
                }).collect(Collectors.toList());

        // 4. 组装当前层级数据
        node.put("processKey", hpi.getProcessDefinitionKey());
        node.put("instanceId", instanceId);
        node.put("lastOutput", vars.getOrDefault(Constants.LAST_OUTPUT, "No Output"));
        node.put("executionPath", path);
        node.put("variables", vars);

        // 5. 【递归关键】寻找 parentInstanceId 等于当前 instanceId 的所有子流程
        List<HistoricProcessInstance> children = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals("parentInstanceId", instanceId)
                .list();

        if (!children.isEmpty()) {
            List<Map<String, Object>> subProcesses = children.stream()
                    .map(child -> collectDeepInfo(child.getId())) // 递归调用自身
                    .collect(Collectors.toList());
            node.put("subProcessResults", subProcesses); // 这里就是 Postman 里的递归结构
        }

        return node;
    }
}
