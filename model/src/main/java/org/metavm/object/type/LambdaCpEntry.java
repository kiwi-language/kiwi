package org.metavm.object.type;

import org.metavm.flow.KlassOutput;
import org.metavm.flow.LambdaRef;

public class LambdaCpEntry extends CpEntry {

    private final LambdaRef lambdaRef;

    public LambdaCpEntry(int index, LambdaRef lambdaRef) {
        super(index);
        this.lambdaRef = lambdaRef;
    }

    @Override
    public LambdaRef getValue() {
        return lambdaRef;
    }

    @Override
    public Object resolve() {
        return lambdaRef.resolve();
    }

    @Override
    public void write(KlassOutput output) {
        lambdaRef.write(output);
    }
}
