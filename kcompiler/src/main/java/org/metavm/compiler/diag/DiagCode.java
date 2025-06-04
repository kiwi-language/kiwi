package org.metavm.compiler.diag;

public enum DiagCode {
    SYMBOL_NOT_FOUND("symbol.not.found"),

    UNEXPECTED_CHAR("unexpected.char"),
    INVALID_ESCAPE("invalid.escape"),
    UNCLOSED_COMMENT("unclosed.comment"),
    INVALID_UNICODE_ESCAPE("invalid.unicode.escape"),
    MALFORMED_FLOAT_LITERAL("malformed.float.literal");

    private final String key;

    DiagCode(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
