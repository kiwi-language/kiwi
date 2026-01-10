package org.manul.util;

public class SessionTimeoutException extends RuntimeException {
    public SessionTimeoutException() {
    }

    public SessionTimeoutException(String message) {
        super(message);
    }
}
