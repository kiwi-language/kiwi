package org.metavm.flow;

public class CodeWriter {

    private final StringBuilder buf = new StringBuilder();

    private int indents = 0;

    public void indent() {
        indents += 1;
    }

    public void unindent() {
        indents -= 1;
    }

    public void write(String str) {
        buf.append(str);
    }

    public void writeNewLine(String line) {
        buf.append('\n').append("    ".repeat(Math.max(0, indents)));
        buf.append(line);
    }

    public String toString() {
        return buf.toString();
    }

}
