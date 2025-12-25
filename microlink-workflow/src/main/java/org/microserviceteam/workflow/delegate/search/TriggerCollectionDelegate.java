package org.microserviceteam.workflow.delegate.search;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.service.WorkflowService;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("triggerCollectionDelegate")
public class TriggerCollectionDelegate extends BaseWorkflowDelegate {

    @Autowired
    private WorkflowService workflowService;

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();

        // 1. 准备传递给采集子流程的变量
        Map<String, Object> subProcessVars = new HashMap<>();
        subProcessVars.put("parentProcessInstanceId", execution.getProcessInstanceId());
        subProcessVars.put("triggerNode", activityId);

        // 从当前流程透传业务数据（例如：contentId 或 q）
        if (activityId.contains("-i")) {
            subProcessVars.put("bizData", execution.getVariable("contentDoc"));
            subProcessVars.put("logType", "INDEX_SYNC_LOG");
        } else {
            subProcessVars.put("bizData", execution.getVariable("q"));
            subProcessVars.put("logType", "SEARCH_QUERY_LOG");
        }

        // 2. 启动数据采集流程 (子流程)
        // 注意：因为主流程节点是同步的，此处启动子流程也是即时触发的
        log.info(">>> [流程衔接] 节点 {} 正在触发采集子流程...", activityId);
        workflowService.start("data-collection-process", subProcessVars, execution);

        String output = "Collection sub-process triggered by " + activityId;
        execution.setVariable(Constants.LAST_OUTPUT, output);

        return output;
    }
}