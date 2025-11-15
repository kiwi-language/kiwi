package org.metavm.http;

import org.metavm.api.EntityFlow;
import org.metavm.api.entity.HttpHeader;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceState;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import java.util.function.Consumer;

public class HttpHeaderImpl implements HttpHeader, NativeEphemeralObject {

    public static final Klass __klass__ = StdKlassRegistry.instance.getKlass(HttpHeaderImpl.class);
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
    public void forEachReference(Consumer<Reference> action) {
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
