package org.microserviceteam.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.config.Constants;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class BaseWorkflowDelegate implements JavaDelegate {

    @Override
    public final void execute(DelegateExecution execution) {
        String nodeId = execution.getCurrentActivityId();

        this.addLog(execution, ">>> 节点 [" + nodeId + "] 开始执行");

        try {
            // 1. 调用子类的业务逻辑
            String output = run(execution);

            // 2. 自动设置最后输出结果
            execution.setVariable(Constants.LAST_OUTPUT, output);

            this.addLog(execution, ">>> 节点 [" + nodeId + "] 执行成功，结果: " + output);

        } catch (Exception e) {
            String errorMsg = "节点 [" + nodeId + "] 执行异常: " + e.getMessage();
            log.error(errorMsg, e);

            // 异常情况下更新状态
            execution.setVariable("isPassed", false);
            execution.setVariable(Constants.LAST_OUTPUT, errorMsg);
            this.addLog(execution, "ERROR: " + errorMsg);

            // 根据业务规范决定是否继续抛出异常（你的规范通常是不抛出，靠 isPassed 控制）
        }
    }

    /**
     * 子类需实现的业务逻辑
     * @return 返回值将自动存入 lastOutput
     */
    protected abstract String run(DelegateExecution execution) throws Exception;

    /**
     * 往流程变量 executionLogs 列表中追加消息
     */
    @SuppressWarnings("unchecked")
    protected void addLog(DelegateExecution execution, String message) {
        List<String> logs = (List<String>) execution.getVariable(Constants.EXECUTION_LOGS);
        if (logs == null) {
            logs = new ArrayList<>();
        }
        // 为了防止并发修改问题，建议重新 setVariable
        logs.add(String.format("[%tT] %s", System.currentTimeMillis(), message));
        execution.setVariable(Constants.EXECUTION_LOGS, logs);
    }
}
