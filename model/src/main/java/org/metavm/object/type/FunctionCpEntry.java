package org.metavm.object.type;

import org.metavm.flow.FunctionRef;
import org.metavm.flow.KlassOutput;

public class FunctionCpEntry extends CpEntry {

    private final FunctionRef functionRef;

    public FunctionCpEntry(int index, FunctionRef functionRef) {
        super(index);
        this.functionRef = functionRef;
    }

    @Override
    public FunctionRef getValue() {
        return functionRef;
    }

    @Override
    public Object resolve() {
        return functionRef.resolve();
    }

    @Override
    public void write(KlassOutput output) {
        functionRef.write(output);
    }

}
