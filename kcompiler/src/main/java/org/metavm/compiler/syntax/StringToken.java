package org.metavm.compiler.syntax;

public class StringToken extends Token {
    private final String value;

    public StringToken(TokenKind kind, int start, int end, String value) {
        super(kind, start, end);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
