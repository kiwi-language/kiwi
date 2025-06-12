package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Formattable;
import org.metavm.compiler.diag.Messages;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public class Token implements Formattable {

    private final TokenKind kind;
    private final int start;
    private final int end;
    @Nullable Token next;

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

    public Token[] split() {
        var tokenKinds = kind.split();
        return new Token[] {
                new Token(tokenKinds[0], start, start + 1),
                new Token(tokenKinds[1], start + 1, end)
        };
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

    @Nullable
    public Token getNext() {
        return next;
    }

    public void setNext(@Nullable Token next) {
        this.next = next;
    }

    public boolean is(TokenKind tk) {
        return kind == tk;
    }

    public boolean isEof() {
        return is(TokenKind.EOF);
    }

    public boolean isNot(TokenKind tk) {
        return kind != tk;
    }

    public boolean isOneOf(TokenKind tk1, TokenKind tk2) {
        return kind == tk1 || kind == tk2;
    }

    public boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3) {
        return kind == tk1 || kind == tk2 || kind == tk3;
    }

    public boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4) {
        return kind == tk1 || kind == tk2 || kind == tk3 || kind == tk4;
    }

    public boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4, TokenKind tk5) {
        return kind == tk1 || kind == tk2 || kind == tk3 || kind == tk4;
    }

    public boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4, TokenKind tk5, TokenKind tk6) {
        return kind == tk1 || kind == tk2 || kind == tk3 || kind == tk4;
    }


    @Override
    public String toString(Locale locale, Messages messages) {
        return kind.toString(locale, messages);
    }
}
