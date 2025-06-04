package org.metavm.compiler.diag;

public enum DiagCode {
    SYMBOL_NOT_FOUND("symbol.not.found"),

    UNEXPECTED_CHAR("unexpected.char"),
    INVALID_ESCAPE("invalid.escape"),
    UNCLOSED_COMMENT("unclosed.comment");

    private final String key;

    DiagCode(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
