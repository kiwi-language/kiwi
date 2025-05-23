package org.metavm.http;

import org.metavm.api.Entity;
import org.metavm.api.EntityFlow;
import org.metavm.api.entity.HttpCookie;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

import java.util.Map;
import java.util.function.Consumer;

@Entity
public class HttpCookieImpl implements HttpCookie, NativeEphemeralObject {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    private final String name;
    private final String value;

    public HttpCookieImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    @EntityFlow
    public String name() {
        return name;
    }

    @Override
    @EntityFlow
    public String value() {
        return value;
    }

    @Override
    public InstanceState state() {
        return state;
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
