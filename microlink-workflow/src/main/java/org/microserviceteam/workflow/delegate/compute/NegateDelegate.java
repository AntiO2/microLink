package org.microserviceteam.workflow.delegate.compute;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.SocialClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("negateDelegate")
public class NegateDelegate implements JavaDelegate {
    @Autowired
    private SocialClient computeClient;

    @Override
    public void execute(DelegateExecution execution) {
        // 1. 获取输入：逻辑同上，保证链式调用的连续性
        Object lastResult = execution.getVariable("lastOutput");
        Double input = (lastResult != null) ?
                Double.valueOf(lastResult.toString()) :
                Double.valueOf(execution.getVariable("initialInput").toString());

        // 2. 调用 Feign 接口执行取反计算
        Double result = computeClient.negate(input);

        // 3. 更新变量
        execution.setVariable("lastOutput", result);
        execution.setVariable("negateOutput", result);

        System.out.println("[Workflow] Negate节点执行完毕: " + input + " -> " + result);
    }
}