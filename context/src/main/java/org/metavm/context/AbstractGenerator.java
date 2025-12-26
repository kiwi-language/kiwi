package org.metavm.context;

import javax.lang.model.element.Name;
import java.util.function.Consumer;

abstract class AbstractGenerator {

    private final StringBuilder buf = new StringBuilder();
    private boolean newline;
    private int indents;

    AbstractGenerator writeln(String s) {
        write(s);
        return writeln();
    }

    AbstractGenerator writeln() {
        buf.append('\n');
        newline = true;
        return this;
    }

    AbstractGenerator write(Name name) {
        return write(name.toString());
    }

    AbstractGenerator write(boolean b) {
        return write(b ? "true" : "false");
    }

    AbstractGenerator write(char c) {
        return write(Character.toString(c));
    }

    AbstractGenerator write(String s) {
        if (newline) {
            writeIndents();
            newline = false;
        }
        for (int i = 0; i < s.length(); i++) {
            var c = s.charAt(i);
            buf.append(c);
            if (c == '\n') {
                if (i == s.length() - 1)
                    newline = true;
                else
                    writeIndents();
            }
        }
        return this;
    }

    <E> AbstractGenerator writeList(Iterable<E> iterable, Consumer<? super E> write, String delimiter) {
        var it = iterable.iterator();
        if (it.hasNext()) {
            write.accept(it.next());
            while (it.hasNext()) {
                write(delimiter);
                write.accept(it.next());
            }
        }
        return this;
    }

    AbstractGenerator indent() {
        indents++;
        return this;
    }

    AbstractGenerator deIndent() {
        indents--;
        return this;
    }

    AbstractGenerator writeStringLit(String s) {
        write("\"");
        for (int i = 0; i < s.length(); i++) {
            var c = s.charAt(i);
            switch (c) {
                case '\b' -> write("\\b");
                case '\t' -> write("\\t");
                case '\n' -> write("\\n");
                case '\f' -> write("\\f");
                case '\r' -> write("\\r");
                case '\"' -> write("\\\"");
                case '\\' -> write("\\\\");
                default -> {
                    if (c < 0x1A) {
                        write("\\u00");
                        write(Character.forDigit(c >> 4 & 0xf, 16));
                        write(Character.forDigit(c & 0xf, 16));
                    } else
                        write(c);
                }
            }
        }
        write("\"");
        return this;
    }

    private void writeIndents() {
        for (int i = 0; i < indents; i++) {
            buf.append("    ");
        }
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
