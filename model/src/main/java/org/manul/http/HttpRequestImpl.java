package org.manul.http;

import lombok.extern.slf4j.Slf4j;
import org.manul.api.EntityFlow;
import org.manul.api.ValueObject;
import org.manul.api.entity.HttpCookie;
import org.manul.api.entity.HttpHeader;
import org.manul.api.entity.HttpRequest;
import org.manul.entity.StdKlassRegistry;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.InstanceState;
import org.manul.object.instance.core.NativeEphemeralObject;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.ClassType;
import org.manul.object.type.Klass;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class HttpRequestImpl implements HttpRequest, ValueObject, NativeEphemeralObject {

    public static final Klass __klass__ = StdKlassRegistry.instance.getKlass(HttpRequestImpl.class);
    private final transient InstanceState state = InstanceState.ephemeral(this);

    public final String method;
    public final String requestPath;
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();
    private Object currentUser = Instances.nullInstance();

    public HttpRequestImpl(String method, String requestPath, List<HttpHeader> headers, List<HttpCookie> cookies) {
        this.method = method;
        this.requestPath = requestPath;
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
    public String getRequestPath() {
        return requestPath;
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
