package org.metavm.object.type;

import org.metavm.flow.KlassOutput;

public class TypeCpEntry extends CpEntry {

    private final Type type;

    public TypeCpEntry(int index, Type type) {
        super(index);
        this.type = type;
    }

    @Override
    public Type getValue() {
        return type;
    }

    @Override
    public Object resolve() {
        return type;
    }

    @Override
    public void write(KlassOutput output) {
        type.write(output);
    }
}
