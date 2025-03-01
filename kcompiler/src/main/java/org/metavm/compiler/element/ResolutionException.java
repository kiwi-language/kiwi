package org.metavm.compiler.element;

public class ResolutionException extends RuntimeException {

    public ResolutionException() {
    }

    public ResolutionException(String message) {
        super(message);
    }

    public ResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
