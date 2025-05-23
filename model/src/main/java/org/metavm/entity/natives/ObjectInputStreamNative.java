package org.metavm.entity.natives;

import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

import java.util.List;
import java.util.Objects;

public class ObjectInputStreamNative implements NativeBase {

    private final ClassInstance instance;
    private final Method readObjectOverride;

    public ObjectInputStreamNative(ClassInstance instance) {
        this.instance = instance;
        readObjectOverride = instance.getInstanceKlass().getMethodByNameAndParamTypes(
                "readObjectOverride", List.of()
        );
    }

    public Value readObject(CallContext callContext) {
        return Objects.requireNonNull(Flows.invoke(readObjectOverride.getRef(), instance, List.of(), callContext));
    }

}
