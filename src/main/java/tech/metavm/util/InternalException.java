package tech.metavm.util;

import tech.metavm.dto.InternalErrorCode;

public class InternalException extends RuntimeException {

    private InternalErrorCode errorCode;

    public InternalException() {
        this((String) null, null);
    }

    public InternalException(InternalErrorCode errorCode, Object...params) {
        super(ResultUtil.formatMessage(errorCode, params));
        this.errorCode = errorCode;
    }

    public InternalException(String message) {
        this(message, null);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        this(null, cause);
    }

    public InternalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InternalErrorCode getErrorCode() {
        return errorCode;
    }

}
