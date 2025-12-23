package org.microserviceteam.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.client.SocialClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("socialInitDelegate")
public class SocialInitDelegate implements JavaDelegate {

    @Autowired
    private SocialClient socialClient;

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        Long contentId = (Long) execution.getVariable("contentId");
        System.out.println("Activiti 编排中：正在调用 Social 服务初始化数据，ID: " + contentId);
        socialClient.initLike(Map.of("contentId", contentId, "status", 0));
    }
}