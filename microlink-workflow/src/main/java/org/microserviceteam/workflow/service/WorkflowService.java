package org.microserviceteam.workflow.service;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
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
@Slf4j
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

        // 1. 执行部署
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel("dynamic_" + System.currentTimeMillis() + ".bpmn20.xml", bpmnModel)
                .name(flowName)
                .deploy();

        // 2. 修复：使用 .list() 而不是 .singleResult()
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .list();

        // 3. 将所有流程 Key 拼接起来返回
        String processKeys = processDefinitions.stream()
                .map(ProcessDefinition::getKey)
                .collect(Collectors.joining(", "));

        Map<String, String> result = new HashMap<>();
        result.put("deploymentId", deployment.getId());
        result.put("processKeys", processKeys); // 返回如: "index-sync-process, data-collect, search-query"
        result.put("status", "Success");

        return result;
    }

    /**
     * 通过消息名称启动流程 (用于 Message Start Event)
     *
     * @param messageName BPMN中定义的message name
     * @param variables   流程变量
     * @param parentExecution 可选的父执行上下文（用于手动关联父子ID）
     * @return 流程实例ID
     */
    public Map<String, Object> startByMessage(String messageName, Map<String, Object> variables, DelegateExecution parentExecution) {
        log.info(">>> 收到消息触发请求. Name: {}, Variables: {}", messageName, variables);

        // 1. 自动关联父流程 ID (如果存在)
        if (parentExecution != null) {
            variables.put("parentInstanceId", parentExecution.getProcessInstanceId());
        }

        // 2. 提取或生成 BusinessKey (建议从变量中提取 key，方便在 ACT_RU_EXECUTION 查看)
        String businessKey = variables.getOrDefault("businessKey", "BK_" + System.currentTimeMillis()).toString();

        // 3. 启动流程
        ProcessInstance pi = runtimeService.startProcessInstanceByMessage(messageName, businessKey, variables);

        // 4. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", pi.getId());
        result.put("processDefinitionId", pi.getProcessDefinitionId());
        result.put("businessKey", pi.getBusinessKey());

        log.info(">>> 流程已启动. InstanceId: {}", pi.getId());
        return collectDeepInfo(pi.getId());
    }

    /**
     * 核心启动与全量结果采集方法
     */
    public Map<String, Object> start(String processKey, Map<String, Object> variables) {
        return start(processKey, variables, null);
    }

    /**
     * 核心启动与全量结果采集方法
     */
    public Map<String, Object> start(String processKey, Map<String, Object> variables, DelegateExecution execution) {
        if(execution != null)
        {
            variables.put("parentInstanceId", execution.getProcessInstanceId());
        }

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
                    m.put("node", activity.getActivityId());
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

        collectChildrenProcessesInfo(children, node);

        children = historyService.createHistoricProcessInstanceQuery()
                .superProcessInstanceId(instanceId) // 引擎自动填充的字段
                .list();

        collectChildrenProcessesInfo(children, node);

        return node;
    }

    private void collectChildrenProcessesInfo(List<HistoricProcessInstance> children, Map<String, Object> node) {
        if (!children.isEmpty()) {
            List<Map<String, Object>> subProcesses = children.stream()
                    .map(child -> collectDeepInfo(child.getId())) // 递归调用自身
                    .collect(Collectors.toList());
            node.put("subProcessResults", subProcesses); // 这里就是 Postman 里的递归结构
        }
    }
}
