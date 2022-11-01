package tech.metavm.dto;

import tech.metavm.util.ResultUtil;

public record Result<T> (int code, String message, T data) {

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

}
