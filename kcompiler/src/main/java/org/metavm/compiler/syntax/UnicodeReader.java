package org.metavm.compiler.syntax;

import static org.metavm.compiler.syntax.Unicodes.EOI;

public class UnicodeReader {
    private final char[] buf;
    private final int len;
    private int pos;
    private int codePoint;

    public UnicodeReader(char[] buf, int len) {
        this.buf = buf;
        this.len = len;
        next();
    }

    public void next() {
        if (codePoint == EOI)
            throw new IllegalStateException("EOF");
        if (!hasNext()) {
            codePoint = EOI;
            return;
        }
        var c = char_();
        if (!Character.isHighSurrogate(c)) {
            pos++;
            codePoint = c;
        }
        else {
            pos++;
            char lo;
            if (hasNext() && Character.isLowSurrogate(lo = char_())) {
                pos++;
                codePoint = Character.toCodePoint(c, lo);
            }
            else
                codePoint = c;
        }
    }

    private boolean hasNext() {
        return pos < len;
    }

    public int get() {
        return codePoint;
    }

    public int pos() {
        return codePoint == EOI ? len : pos - 1;
    }

    private char char_() {
        return buf[pos];
    }

    public void reset(int pos) {
        this.pos = pos;
        codePoint = -1;
        next();
    }
}
