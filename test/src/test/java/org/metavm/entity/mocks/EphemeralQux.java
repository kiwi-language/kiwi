package org.metavm.entity.mocks;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceState;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import java.util.Map;
import java.util.function.Consumer;

public class EphemeralQux implements NativeEphemeralObject  {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    @Override
    public InstanceState state() {
        return state;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }
}
