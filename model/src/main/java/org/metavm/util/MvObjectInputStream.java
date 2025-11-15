package org.metavm.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Entity(ephemeral = true)
public class MvObjectInputStream implements NativeEphemeralObject {

    public static MvObjectInputStream create(InstanceInput input) {
        try {
            return new MvObjectInputStream(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    private final transient InstanceInput input;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    protected MvObjectInputStream(InstanceInput input) throws IOException {
        this.input = input;
    }

    @Override
    public InstanceState state() {
        return state;
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

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }
}
