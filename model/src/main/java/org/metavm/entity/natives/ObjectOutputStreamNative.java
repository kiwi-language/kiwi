package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Types;

import java.util.List;

@Slf4j
public class ObjectOutputStreamNative extends NativeBase {

    private final ClassInstance instance;
    private final Method writeObjectOverride;

    public ObjectOutputStreamNative(ClassInstance instance) {
        this.instance = instance;
        writeObjectOverride = instance.getKlass().getMethodByNameAndParamTypes(
                "writeObjectOverride", List.of(Types.getNullableAnyType())
        );
    }

    public void writeObject(Value object, CallContext callContext) {
        Flows.invoke(writeObjectOverride, instance, List.of(object), callContext);
    }

}
