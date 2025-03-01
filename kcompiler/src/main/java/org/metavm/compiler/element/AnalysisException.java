package org.metavm.compiler.element;

public class AnalysisException extends RuntimeException {

    public AnalysisException() {
    }

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
