package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

public class MvObjectNative implements NativeBase {

    private final ClassInstance instance;

    public MvObjectNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value MvObject(CallContext callContext) {
        return instance.getReference();
    }

}
