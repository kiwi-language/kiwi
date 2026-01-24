package org.manul.common;

import org.jsonk.Json;
import org.manul.util.ResultUtil;

@Json
public record ErrorResponse(String code, String message) {

    public static ErrorResponse create(ErrorCode errorCode, Object... params) {
        return new ErrorResponse(errorCode.name(), ResultUtil.formatMessage(errorCode, params));
    }

}
