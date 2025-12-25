package org.microserviceteam.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 成功方法
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    // 使用枚举的错误方法
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMessage(resultCode.getMessage());
        return r;
    }

    // 支持自定义消息的错误方法（保留灵活性）
    public static <T> Result<T> error(ResultCode resultCode, String customMessage) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMessage(customMessage);
        return r;
    }
}
