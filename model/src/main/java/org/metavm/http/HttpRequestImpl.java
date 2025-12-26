package org.metavm.http;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.EntityFlow;
import org.metavm.api.ValueObject;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpRequest;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceState;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;

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
