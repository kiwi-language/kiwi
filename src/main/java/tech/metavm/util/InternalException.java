package tech.metavm.util;

import tech.metavm.dto.InternalErrorCode;

public class InternalException extends RuntimeException {

    private InternalErrorCode errorCode;

    public InternalException() {
    }

    public InternalException(InternalErrorCode errorCode, Object...params) {
        super(ResultUtil.formatMessage(errorCode, params));
        this.errorCode = errorCode;
    }

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }

    public InternalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InternalErrorCode getErrorCode() {
        return errorCode;
    }

}
