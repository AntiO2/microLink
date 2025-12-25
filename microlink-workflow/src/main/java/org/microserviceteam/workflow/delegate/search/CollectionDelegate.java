package org.microserviceteam.workflow.delegate.search;

import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("collectionDelegate")
public class CollectionDelegate extends BaseWorkflowDelegate {

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        String output = "";

        // 获取由 Trigger 传入的变量
        String logType = (String) execution.getVariable("logType");

        switch (activityId) {
            case "task-clean":
                log.info(">>> [采集子流程] 正在清洗日志类型: {}", logType);
                output = "Log cleaned for " + logType;
                break;
            case "task-db-save":
                log.info(">>> [采集子流程] 正在存入数据库...");
                output = "Saved to DB";
                break;
            case "task-analysis-save":
                log.info(">>> [采集子流程] 正在存入分析引擎...");
                output = "Saved to Analysis Engine";
                break;
        }

        execution.setVariable(Constants.LAST_OUTPUT, output);
        return output;
    }
}
