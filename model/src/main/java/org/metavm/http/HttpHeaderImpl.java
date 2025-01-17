package org.metavm.http;

import org.metavm.api.Entity;
import org.metavm.api.entity.HttpHeader;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

import java.util.Map;
import java.util.function.Consumer;

@Entity(systemAPI = true)
public class HttpHeaderImpl implements HttpHeader, NativeEphemeralObject {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    public final String name;
    public final String value;

    public HttpHeaderImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public InstanceState state() {
        return state;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    public Value name(CallContext callContext) {
        return Instances.stringInstance(name());
    }

    public Value value(CallContext callContext) {
        return Instances.stringInstance(value());
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
