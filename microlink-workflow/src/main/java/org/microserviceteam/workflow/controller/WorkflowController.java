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

    /**
     * 极简通用消息触发器
     * 无论业务是搜索、订单还是审批，只要 BPMN 里定义了 messageName，就走这一个接口
     */
    @PostMapping("/message/{messageName}")
    public Result<Map<String, Object>> startByMessage(@PathVariable String messageName,
                                                      @RequestBody Map<String, Object> variables) {
        // 调用 Service，parentExecution 传 null 表示这是根流程启动
        Map<String, Object> result = workflowService.startByMessage(messageName, variables, null);

        return Result.success(result);
    }
}