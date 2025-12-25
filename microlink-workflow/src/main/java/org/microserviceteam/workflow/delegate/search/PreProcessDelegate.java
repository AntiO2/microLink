package org.microserviceteam.workflow.delegate.search;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * 数据预处理代理类
 * 对应 BPMN 中的 Task_0gldkkg (数据预处理) 节点
 */
@Slf4j
@Component("preProcessDelegate")
public class PreProcessDelegate extends BaseWorkflowDelegate {

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        try {
            // 1. 获取数据
            Object contentDoc = execution.getVariable("contentDoc");

            // 2. 模拟校验逻辑（例如：标题不能为空）
            if (contentDoc instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) contentDoc;
                if (data.get("title") == null || "".equals(data.get("title"))) {
                    // 校验失败
                    execution.setVariable("isDataValid", false);
                    return "预处理失败：标题缺失";
                }

                // 校验通过，执行清洗...
                execution.setVariable("isDataValid", true);
                return "预处理成功";
            }

            execution.setVariable("isDataValid", false);
            return "预处理失败：无效的数据格式";

        } catch (Exception e) {
            execution.setVariable("isDataValid", false);
            return "预处理异常：" + e.getMessage();
        }
    }
}