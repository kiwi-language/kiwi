package org.metavm.object.type;

import org.metavm.flow.KlassOutput;

public class FieldCpEntry extends CpEntry {

    private final FieldRef fieldRef;

    public FieldCpEntry(int index, FieldRef fieldRef) {
        super(index);
        this.fieldRef = fieldRef;
    }

    @Override
    public FieldRef getValue() {
        return fieldRef;
    }

    @Override
    public void write(KlassOutput output) {
        fieldRef.write(output);
    }
}
