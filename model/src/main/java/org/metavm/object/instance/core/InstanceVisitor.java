package org.metavm.object.instance.core;

import org.metavm.entity.Entity;

public abstract class InstanceVisitor<R> {

    public R visitInstance(Instance instance) {
        throw new UnsupportedOperationException();
    }

    public R visitClassInstance(ClassInstance instance) {
        return visitInstance(instance);
    }

    public R visitArrayInstance(ArrayInstance instance) {
        return visitInstance(instance);
    }

    public R visitEntity(Entity entity) {
        return visitClassInstance(entity);
    }

    public R visitNativeEphemeralObject(NativeEphemeralObject nativeEphemeralObject) {
        return visitInstance(nativeEphemeralObject);
    }
}
