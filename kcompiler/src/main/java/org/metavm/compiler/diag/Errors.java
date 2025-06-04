package org.metavm.compiler.diag;

public class Errors {

    public static final Error UNCLOSED_COMMENT = create(DiagCode.UNCLOSED_COMMENT);

    public static Error symbolNotFound(String sym) {
        return create(DiagCode.SYMBOL_NOT_FOUND, sym);
    }

    public static Error unexpectedChar(String s) {
        return create(DiagCode.UNEXPECTED_CHAR, s);
    }

    public static Error invalidEscape(String s) {
        return create(DiagCode.INVALID_ESCAPE, s);
    }

    private static Error create(DiagCode code, Object...args) {
        return new Error(code, args);
    }

}
