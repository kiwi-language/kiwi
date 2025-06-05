package org.metavm.compiler.syntax;

import java.util.Objects;

public class StringToken extends Token {
    private final String value;

    public StringToken(TokenKind kind, int start, int end, String value) {
        super(kind, start, end);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        StringToken that = (StringToken) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "StringToken{" +
                "kind=" + getKind() +
                ", start=" + getStart() +
                ", end=" + getEnd() +
                ", value='" + value + '\'' +
                '}';
    }
}
