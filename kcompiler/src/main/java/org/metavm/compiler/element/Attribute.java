package org.metavm.compiler.element;

import org.metavm.util.MvOutput;

public record Attribute(String name, String value) {

    public void write(MvOutput output) {
        output.writeUTF(name);
        output.writeUTF(value);
    }

}
