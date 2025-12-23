package org.microserviceteam.workflow.controller;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.microserviceteam.common.Result;
import org.microserviceteam.workflow.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @PostMapping("/start/{processKey}")
    public Result<Map<String, Object>> start(@PathVariable String processKey, @RequestBody Map<String, Object> variables) {
        org.activiti.api.process.model.ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withVariables(variables)
                .build());
        String instanceId = processInstance.getId();
        // 同步获取结果 (lastOutput)
        // 假设你的 Delegate 最终将结果存入了变量 "result" 或 "lastOutput"
        Object lastOutput = null;

        HistoricVariableInstance historicVar = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instanceId)
                .variableName(Constants.LAST_OUTPUT)
                .singleResult();

        if (historicVar != null) {
            lastOutput = historicVar.getValue();
        }

        Map<String, Object> processInfo = new HashMap<>();
        processInfo.put("processInstanceId", instanceId);
        processInfo.put("processDefinitionKey", processKey);
        processInfo.put("status", processInstance.getStatus().name());
        processInfo.put(Constants.LAST_OUTPUT, lastOutput);

        return Result.success(processInfo);
    }
}