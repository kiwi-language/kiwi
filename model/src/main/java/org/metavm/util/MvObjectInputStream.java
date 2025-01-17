package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Entity(ephemeral = true)
public class MvObjectInputStream extends ObjectInputStream implements NativeEphemeralObject {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static MvObjectInputStream create(InstanceInput input) {
        try {
            return new MvObjectInputStream(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient InstanceInput input;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    protected MvObjectInputStream(InstanceInput input) throws IOException {
        this.input = input;
    }

    public InstanceInput getInput() {
        return input;
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
    public void buildJson(Map<String, Object> map) {
        map.put("input", this.getInput());
        map.put("objectInputFilter", this.getObjectInputFilter());
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
