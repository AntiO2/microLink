package org.microserviceteam.workflow.util;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ProcessVariableUtil {

    private static final Logger logger = LoggerFactory.getLogger(ProcessVariableUtil.class);

    /**
     * 安全获取字符串变量，若不存在则返回默认值
     */
    public static String getString(DelegateExecution execution, String variableName, String defaultValue) {
        return Optional.ofNullable(execution.getVariable(variableName))
                .map(Object::toString)
                .orElseGet(() -> {
                    logger.warn(">>> [变量工具] 变量 {} 缺失，使用默认值: {}", variableName, defaultValue);
                    return defaultValue;
                });
    }

    /**
     * 强类型获取变量（泛型转换）
     * @param type 期望的类型 Class
     */
    public static <T> T getVariable(DelegateExecution execution, String variableName, Class<T> type) {
        Object value = execution.getVariable(variableName);
        if (value == null) {
            logger.warn(">>> [变量工具] 变量 {} 为空", variableName);
            return null;
        }

        if (type.isInstance(value)) {
            return type.cast(value);
        }

        logger.error(">>> [变量工具] 变量 {} 类型不匹配. 期望: {}, 实际: {}",
                variableName, type.getSimpleName(), value.getClass().getSimpleName());
        return null;
    }

    /**
     * 安全获取布尔值（常用于网关判断）
     */
    public static boolean getBoolean(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        return Boolean.TRUE.equals(value);
    }
}