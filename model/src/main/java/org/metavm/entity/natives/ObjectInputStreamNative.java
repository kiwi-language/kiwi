package org.metavm.entity.natives;

import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

import java.util.List;
import java.util.Objects;

public class ObjectInputStreamNative extends NativeBase {

    private final ClassInstance instance;
    private final Method readObjectOverride;

    public ObjectInputStreamNative(ClassInstance instance) {
        this.instance = instance;
        readObjectOverride = instance.getKlass().getMethodByNameAndParamTypes(
                "readObjectOverride", List.of()
        );
    }

    public Value readObject(CallContext callContext) {
        return Objects.requireNonNull(Flows.invoke(readObjectOverride, instance, List.of(), callContext));
    }

}
