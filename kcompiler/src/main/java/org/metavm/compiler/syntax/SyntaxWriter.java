package org.metavm.compiler.syntax;

import org.metavm.compiler.element.SymName;

import java.util.List;

public class SyntaxWriter {

    private int indents;
    private boolean newLine;
    private final StringBuilder sb = new StringBuilder();

    void indent() {
        indents++;
    }

    void deIndent() {
        indents--;
    }

    void writeln(String s) {
        write(s);
        writeln();
    }

    void write(SymName name) {
        write(name.toString());
    }

    void write(String s) {
        if (newLine) {
            sb.append("    ".repeat(Math.max(0, indents)));
            newLine = false;
        }
        sb.append(s);
    }

    void writeln() {
        sb.append('\n');
        newLine = true;
    }

    void write(Node node) {
        node.write(this);
    }

    void write(List<? extends Node> nodes) {
        write(nodes, ", ");
    }

    void write(List<? extends Node> nodes, String delimiter) {
        if (!nodes.isEmpty()) {
            var it = nodes.iterator();
            write(it.next());
            while (it.hasNext()) {
                write(delimiter);
                write(it.next());
            }
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
