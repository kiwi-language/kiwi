package org.metavm.object.type;

import org.metavm.flow.KlassOutput;
import org.metavm.object.instance.core.Value;

public class ValueCpEntry extends CpEntry {

    private final Value value;

    public ValueCpEntry(int index, Value value) {
        super(index);
        this.value = value;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public void write(KlassOutput output) {
        value.write(output);
    }
}
