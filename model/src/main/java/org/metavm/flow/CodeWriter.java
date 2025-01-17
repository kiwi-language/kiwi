package org.metavm.flow;

public class CodeWriter {

    private final StringBuilder buf = new StringBuilder();

    private int indents = 0;
    private boolean newLine = true;

    public void indent() {
        indents += 1;
    }

    public void unindent() {
        indents -= 1;
    }

    public void write(String str) {
        if (newLine) {
            buf.append("    ".repeat(Math.max(0, indents)));
            newLine = false;
        }
        buf.append(str);
    }

    public void writeln() {
        buf.append('\n');
        newLine = true;
    }

    public void writeln(String line) {
        write(line);
        writeln();
    }

    public String toString() {
        return buf.toString();
    }

}
