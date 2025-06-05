package org.metavm.compiler.syntax;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Token token = (Token) object;
        return start == token.start && end == token.end && kind == token.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, start, end);
    }

    @Override
    public String toString() {
        return "Token{" +
                "kind=" + kind +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
