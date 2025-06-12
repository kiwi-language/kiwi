package org.metavm.compiler.syntax;

import java.util.Objects;

public class NumberToken extends StringToken {
    private final int radix;

    public NumberToken(TokenKind kind, int start, int end, String value, int radix) {
        super(kind, start, end, value);
        this.radix = radix;
    }

    @Override
    public int hashCode() {
        return Objects.hash(radix);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        NumberToken that = (NumberToken) object;
        return radix == that.radix;
    }

    public int getRadix() {
        return radix;
    }

    @Override
    public String toString() {
        return "NumberToken{" +
                "kind=" + getKind() +
                ", start=" + getStart() +
                ", end=" + getEnd() +
                ", value='" + getValue() + '\'' +
                ", radix=" + radix +
                '}';
    }
}
