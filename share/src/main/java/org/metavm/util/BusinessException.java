package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;

@Slf4j
public class BusinessException extends RuntimeException {


    private final ErrorCode errorCode;
    private final Object[] params;

    public BusinessException(ErrorCode errorCode, Object... params) {
        super(ResultUtil.formatMessage(errorCode, params));
        this.errorCode = errorCode;
        this.params = params;
    }

    public static BusinessException invalidParams(String detail) {
        return new BusinessException(ErrorCode.INVALID_PARAMETERS, detail);
    }


    public static BusinessException tooManyFields() {
        throw new BusinessException(ErrorCode.TOO_MANY_FIELDS);
    }

    public static BusinessException loginNameNotFound(String loginName) {
        return new BusinessException(ErrorCode.LOGIN_NAME_NOT_FOUND, loginName);
    }

    public static BusinessException invalidName(String name) {
        return new BusinessException(ErrorCode.INVALID_SYMBOL_NAME, name);
    }

    public static BusinessException invalidExpression(String reason) {
        return new BusinessException(ErrorCode.EXPRESSION_INVALID, reason);
    }

    public static BusinessException userNotFound(String id) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND, id);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }

}

