package org.metavm.compiler.syntax;

public class Token {

    private final TokenKind kind;
    private final int start;
    private final int end;

    public Token(TokenKind kind, int start, int end) {
        this.kind = kind;
        this.start = start;
        this.end = end;
    }

    public TokenKind getKind() {
        return kind;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
