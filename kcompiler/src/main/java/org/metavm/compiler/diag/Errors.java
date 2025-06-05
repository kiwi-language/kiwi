package org.metavm.compiler.diag;

public class Errors {

    public static final Error UNCLOSED_COMMENT = create("unclosed.comment");
    public static final Error MALFORMED_FLOAT_LITERAL = create("malformed.float.literal");
    public static Error illegalEscChar = create("illegal.esc.char");

    public static Error symbolNotFound(String sym) {
        return create("symbol.not.found", sym);
    }

    public static Error unexpectedChar = create("unexpected.char");

    public static Error invalidUnicodeEscape = create("invalid.unicode.escape");

    private static Error create(String code, Object...args) {
        return new Error(code, args);
    }

}
