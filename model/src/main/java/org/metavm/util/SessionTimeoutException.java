package org.metavm.util;

public class SessionTimeoutException extends RuntimeException {
    public SessionTimeoutException() {
    }

    public SessionTimeoutException(String message) {
        super(message);
    }
}
