package org.microserviceteam.common;
import lombok.Getter;
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),

    // 社交业务校验相关 (4000-4999)
    DUPLICATE_LIKE(4001, "无法重复点赞"),
    SENSITIVE_CONTENT(4002, "内容包含敏感词汇"),
    USER_BLACK_LIST(4003, "已被对方拉黑，无法操作"),

    // 系统级错误 (5000+)
    SYSTEM_ERROR(5000, "系统繁忙，请稍后再试"),
    VALIDATION_FAILED(5001, "业务校验失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}