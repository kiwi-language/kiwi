package org.metavm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.util.ResultUtil;

public final class Result<T> {
    private final int code;
    private final String message;
    private final T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, null, data);
    }

    public static Result<Void> voidSuccess() {
        return success(null);
    }

    public static <T> Result<T> failure(ErrorCode resultType) {
        return new Result<>(resultType.code(), resultType.message(), null);
    }

    public static <T> Result<T> failure(ErrorCode resultType, Object[] params) {
        return new Result<>(resultType.code(), ResultUtil.formatMessage(resultType, params), null);
    }

    public int getCode() {
        return code;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return code == 0;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

}
