package org.metavm.compiler.element;

import org.metavm.compiler.generate.KlassOutput;

public record Attribute(String name, String value) {

    public void write(KlassOutput output) {
        output.writeUTF(name);
        output.writeUTF(value);
    }

}
