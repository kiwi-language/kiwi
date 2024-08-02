package org.metavm.util;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.NullValue;

public class ThreadLocalBuiltinInstanceHolder implements BuiltinInstanceHolder {

    private final ThreadLocal<BuiltinInstanceHolder> THREAD_LOCAL = ThreadLocal.withInitial(GlobalBuiltinInstanceHolder::new);

    @Override
    public NullValue getNullInstance() {
        return THREAD_LOCAL.get().getNullInstance();
    }

    @Override
    public void setNullInstance(NullValue nullInstance) {
        THREAD_LOCAL.get().setNullInstance(nullInstance);
    }

    @Override
    public BooleanValue getTrueInstance() {
        return THREAD_LOCAL.get().getTrueInstance();
    }

    @Override
    public void setTrueInstance(BooleanValue trueInstance) {
        THREAD_LOCAL.get().setTrueInstance(trueInstance);
    }

    @Override
    public BooleanValue getFalseInstance() {
        return THREAD_LOCAL.get().getFalseInstance();
    }

    @Override
    public void setFalseInstance(BooleanValue falseInstance) {
        THREAD_LOCAL.get().setFalseInstance(falseInstance);
    }
}
