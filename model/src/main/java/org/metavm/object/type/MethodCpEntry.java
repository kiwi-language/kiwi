package org.metavm.object.type;

import org.metavm.flow.KlassOutput;
import org.metavm.flow.MethodRef;

public class MethodCpEntry extends CpEntry {

    private final MethodRef methodRef;

    public MethodCpEntry(int index, MethodRef methodRef) {
        super(index);
        this.methodRef = methodRef;
    }

    @Override
    public MethodRef getValue() {
        return methodRef;
    }

    @Override
    public void write(KlassOutput output) {
        methodRef.write(output);
    }
}
