package org.microserviceteam.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.SocialClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("squareDelegate")
public class SquareDelegate implements JavaDelegate {
    @Autowired
    private SocialClient computeClient;

    @Override
    public void execute(DelegateExecution execution) {
        // 1. 获取输入：优先取上一个节点的输出，取不到则取初始输入的 JSON 字段
        Object lastResult = execution.getVariable("lastOutput");
        Double input = (lastResult != null) ?
                Double.valueOf(lastResult.toString()) :
                Double.valueOf(execution.getVariable("initialInput").toString());

        // 2. 调用 Feign 接口执行平方计算
        Double result = computeClient.square(input);

        // 3. 更新变量：更新通用输出标识 lastOutput，并记录节点特有输出
        execution.setVariable("lastOutput", result);
        execution.setVariable("squareOutput", result);

        System.out.println("[Workflow] Square节点执行完毕: " + input + " -> " + result);
    }
}