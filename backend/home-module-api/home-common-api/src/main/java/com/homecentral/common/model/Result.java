package com.homecentral.common.model;

import org.springframework.data.domain.Page;

public class Result<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.success = true;
        result.code = "OK";
        result.message = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> bizError(String code, String message) {
        Result<T> result = new Result<>();
        result.success = false;
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<Page<T>> page(Page<T> page) {
        Result<Page<T>> result = new Result<>();
        result.success = true;
        result.code = "OK";
        result.message = "success";
        result.data = page;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
