package tech.metavm.util;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;

public class GlobalBuiltinInstanceHolder implements BuiltinInstanceHolder {

    private NullInstance nullInstance;
    private BooleanInstance trueInstance;
    private BooleanInstance falseInstance;

    @Override
    public NullInstance getNullInstance() {
        return nullInstance;
    }

    @Override
    public void setNullInstance(NullInstance nullInstance) {
        this.nullInstance = nullInstance;
    }

    @Override
    public BooleanInstance getTrueInstance() {
        return trueInstance;
    }

    @Override
    public void setTrueInstance(BooleanInstance trueInstance) {
        this.trueInstance = trueInstance;
    }

    @Override
    public BooleanInstance getFalseInstance() {
        return falseInstance;
    }

    @Override
    public void setFalseInstance(BooleanInstance falseInstance) {
        this.falseInstance = falseInstance;
    }
}
