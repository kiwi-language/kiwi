package tech.metavm.util;

public class InternalException extends RuntimeException {

    public InternalException() {
        this(null, null);
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


}
