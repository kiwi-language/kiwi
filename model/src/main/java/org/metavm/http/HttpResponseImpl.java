package org.metavm.http;

import org.metavm.api.Entity;
import org.metavm.api.EntityFlow;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Entity(ephemeral = true)
public class HttpResponseImpl implements HttpResponse, NativeEphemeralObject {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    private final List<HttpHeader> headers = new ArrayList<>();

    private final List<HttpCookie> cookies = new ArrayList<>();

    @Override
    @EntityFlow
    public void addCookie(String name, String value) {
        cookies.removeIf(c -> c.name().equals(name));
        cookies.add(new HttpCookieImpl(name, value));
    }

    @Override
    @EntityFlow
    public void addHeader(String name, String value) {
        headers.removeIf(h -> h.name().equals(name));
        headers.add(new HttpHeaderImpl(name, value));
    }

    @Override
    @EntityFlow
    public List<HttpCookie> getCookies() {
        return Collections.unmodifiableList(cookies);
    }

    @Override
    @EntityFlow
    public List<HttpHeader> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    @Override
    public InstanceState state() {
        return state;
    }

    public Value addCookie(Value name, Value value, CallContext callContext) {
        addCookie(name.stringValue(), value.stringValue());
        return Instances.nullInstance();
    }

    public Value addHeader(Value name, Value value, CallContext callContext) {
        addHeader(name.stringValue(), value.stringValue());
        return Instances.nullInstance();
    }

    public Value getCookies(CallContext callContext) {
        return Instances.list(StdKlass.httpCookie.type(),
                Utils.map(getCookies(), c -> (Value) c));
    }

    public Value getHeaders(CallContext callContext) {
        return Instances.list(StdKlass.httpHeader.type(),
                Utils.map(getHeaders(), h -> (Value) h));
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("cookies", this.getCookies());
        map.put("headers", this.getHeaders());
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
