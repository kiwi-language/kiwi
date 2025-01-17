package org.metavm.object.instance.core;

public class RemovalFailureException extends RuntimeException {

    public RemovalFailureException() {
    }

    public RemovalFailureException(String message) {
        super(message);
    }

    public RemovalFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
