package org.metavm.util;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.NullValue;

public class GlobalBuiltinInstanceHolder implements BuiltinInstanceHolder {

    private NullValue nullInstance;
    private BooleanValue trueInstance;
    private BooleanValue falseInstance;

    @Override
    public NullValue getNullInstance() {
        return nullInstance;
    }

    @Override
    public void setNullInstance(NullValue nullInstance) {
        this.nullInstance = nullInstance;
    }

    @Override
    public BooleanValue getTrueInstance() {
        return trueInstance;
    }

    @Override
    public void setTrueInstance(BooleanValue trueInstance) {
        this.trueInstance = trueInstance;
    }

    @Override
    public BooleanValue getFalseInstance() {
        return falseInstance;
    }

    @Override
    public void setFalseInstance(BooleanValue falseInstance) {
        this.falseInstance = falseInstance;
    }
}
