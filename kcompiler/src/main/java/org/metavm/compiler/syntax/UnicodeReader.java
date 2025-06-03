package org.metavm.compiler.syntax;

import static org.metavm.compiler.syntax.Unicodes.EOI;

public class UnicodeReader {
    private final char[] buf;
    private int pos;
    private int codePoint;

    public UnicodeReader(char[] buf) {
        this.buf = buf;
        if (hasNext())
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
        return pos < buf.length;
    }

    public int get() {
        if (codePoint == -1)
            throw new IllegalStateException("EOF");
        return codePoint;
    }

    public int pos() {
        return pos;
    }

    private char char_() {
        return buf[pos];
    }

}
