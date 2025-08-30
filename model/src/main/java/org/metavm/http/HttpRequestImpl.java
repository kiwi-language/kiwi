package org.metavm.http;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.api.EntityFlow;
import org.metavm.api.ValueObject;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpRequest;
import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Entity
@Slf4j
public class HttpRequestImpl implements HttpRequest, ValueObject, NativeEphemeralObject {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    public final String method;
    public final String requestURI;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();
    private Object currentUser = Instances.nullInstance();

    public HttpRequestImpl(String method, String requestURI, List<HttpHeader> headers, List<HttpCookie> cookies) {
        this.method = method;
        this.requestURI = requestURI;
        headers.forEach(h -> this.headers.put(h.name().toLowerCase(), h.value()));
        cookies.forEach(c -> this.cookies.put(c.name(), c.value()));
    }

    @Override
    @EntityFlow
    public String getMethod() {
        return method;
    }

    @Override
    @EntityFlow
    public String getRequestURI() {
        return requestURI;
    }

    @Nullable
    @Override
    @EntityFlow
    public String getCookie(String name) {
        return cookies.get(name);
    }

    @Nullable
    @Override
    @EntityFlow
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    @Override
    @EntityFlow
    public void setCurrentUser(Object currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    @EntityFlow
    public Object getCurrentUser() {
        return currentUser;
    }

    @Override
    public InstanceState state() {
        return state;
    }

    public Value getMethod(CallContext callContext) {
        return Instances.stringInstance(method);
    }

    public Value getRequestURI(CallContext callContext) {
        return Instances.stringInstance(requestURI);
    }

    public Value getCookie(Value name, CallContext callContext) {
        var n = name.stringValue();
        var c = getCookie(n);
        return c != null ? Instances.stringInstance(c) : Instances.nullInstance();
    }

    public Value getHeader(Value name, CallContext callContext) {
        var n = name.stringValue();
        var h = getHeader(n);
        return h != null ? Instances.stringInstance(h) : Instances.nullInstance();
    }

    public Value getCurrentUser(CallContext callContext) {
        return (Value) getCurrentUser();
    }

    public Value setCurrentUser(Value currentUser, CallContext callContext) {
        setCurrentUser(currentUser);
        return Instances.nullInstance();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("method", this.getMethod());
        map.put("requestURI", this.getRequestURI());
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
