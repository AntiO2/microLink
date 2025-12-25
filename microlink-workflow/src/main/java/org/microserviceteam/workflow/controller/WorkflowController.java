package org.microserviceteam.workflow.controller;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.microserviceteam.common.Result;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/start/{processKey}")
    public Result<Map<String, Object>> start(@PathVariable String processKey,
                                             @RequestBody Map<String, Object> variables) {

        // 核心逻辑已下推：WorkflowService.start 会递归处理 lastOutput 和子流程回溯
        Map<String, Object> processDeepInfo = workflowService.start(processKey, variables);

        return Result.success(processDeepInfo);
    }
}