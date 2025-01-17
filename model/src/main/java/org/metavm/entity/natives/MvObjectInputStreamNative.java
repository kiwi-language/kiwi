package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.InstanceInput;
import org.metavm.util.Instances;
import org.metavm.util.MvObjectInputStream;

import java.io.IOException;
import java.util.Objects;

public class MvObjectInputStreamNative extends NativeBase {

    private final InstanceInput input;

    public MvObjectInputStreamNative(ClassInstance instance) {
        input = ((MvObjectInputStream) instance).getInput();
    }

    public Value readObject(CallContext callContext) {
        return input.readValue();
    }

    public Value defaultReadObject(CallContext callContext) {
        var inst = (ClassInstance) Objects.requireNonNull(input.getParent());
        inst.defaultRead(input);
        return Instances.nullInstance();
    }

    public Value readUTF(CallContext callContext) {
        return Instances.stringInstance(input.readUTF());
    }

    public Value readLong(CallContext callContext) throws IOException {
        return Instances.longInstance(input.readLong());
    }

    public Value readInt(CallContext callContext) throws IOException {
        return Instances.intInstance(input.readInt());
    }

}
