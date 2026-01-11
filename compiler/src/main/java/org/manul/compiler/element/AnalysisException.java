package org.manul.compiler.element;

import org.manul.compiler.util.CompilationException;

public class AnalysisException extends CompilationException {

    public AnalysisException() {
    }

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
