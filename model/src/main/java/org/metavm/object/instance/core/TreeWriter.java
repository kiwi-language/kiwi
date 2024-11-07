package org.metavm.object.instance.core;

public class TreeWriter {

    private final StringBuilder buf = new StringBuilder();
    private int indent;

    public void write(String s) {
        buf.append("    ".repeat(Math.max(0, indent)));
        buf.append(s);
    }

    public void writeLine(String line) {
        write(line);
        buf.append("\n");
    }

    public void indent() {
        indent++;
    }

    public void deIndent() {
        if(indent > 0)
            indent--;
        else
            throw new IllegalStateException("Indentation level is already 0");
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
