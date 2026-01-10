package org.manul.common;

import org.jsonk.Json;
import org.manul.util.ResultUtil;

@Json
public final class ErrorResponse {
    private final int code;
    private final String message;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse create(ErrorCode resultType, Object...params) {
        return new ErrorResponse(resultType.code(), ResultUtil.formatMessage(resultType, params));
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
