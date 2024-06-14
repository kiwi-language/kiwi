package org.metavm.util;

import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.NullInstance;

public class ThreadLocalBuiltinInstanceHolder implements BuiltinInstanceHolder {

    private final ThreadLocal<BuiltinInstanceHolder> THREAD_LOCAL = ThreadLocal.withInitial(GlobalBuiltinInstanceHolder::new);

    @Override
    public NullInstance getNullInstance() {
        return THREAD_LOCAL.get().getNullInstance();
    }

    @Override
    public void setNullInstance(NullInstance nullInstance) {
        THREAD_LOCAL.get().setNullInstance(nullInstance);
    }

    @Override
    public BooleanInstance getTrueInstance() {
        return THREAD_LOCAL.get().getTrueInstance();
    }

    @Override
    public void setTrueInstance(BooleanInstance trueInstance) {
        THREAD_LOCAL.get().setTrueInstance(trueInstance);
    }

    @Override
    public BooleanInstance getFalseInstance() {
        return THREAD_LOCAL.get().getFalseInstance();
    }

    @Override
    public void setFalseInstance(BooleanInstance falseInstance) {
        THREAD_LOCAL.get().setFalseInstance(falseInstance);
    }
}
