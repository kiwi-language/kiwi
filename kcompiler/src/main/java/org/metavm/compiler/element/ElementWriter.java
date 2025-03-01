package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;

import java.util.Collection;
import java.util.List;

public class ElementWriter {

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

    public void write(SymName name) {
        write(name.toString());
    }

    public void write(String s) {
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

    void write(Element node) {
        node.write(this);
    }

    public void writeType(Type type) {
        type.write(this);
    }

    public void writeTypes(List<? extends Type> types) {
        writeTypes(types, ", ");
    }

    public void writeTypes(Collection<? extends Type> types, String delimiter) {
        if (!types.isEmpty()) {
            var it = types.iterator();
            writeType(it.next());
            while (it.hasNext()) {
                write(delimiter);
                writeType(it.next());
            }
        }
    }

    void write(List<? extends Element> nodes) {
        write(nodes, ", ");
    }

    void write(List<? extends Element> nodes, String delimiter) {
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
