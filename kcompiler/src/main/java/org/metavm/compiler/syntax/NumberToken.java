package org.metavm.compiler.syntax;

public class NumberToken extends StringToken {
    private final int radix;

    public NumberToken(TokenKind kind, int start, int end, String value, int radix) {
        super(kind, start, end, value);
        this.radix = radix;
    }

    public int getRadix() {
        return radix;
    }
}
