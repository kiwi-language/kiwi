package org.metavm.http;

import org.metavm.api.EntityFlow;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceState;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HttpResponseImpl implements HttpResponse, NativeEphemeralObject {

    public static final Klass __klass__ = StdKlassRegistry.instance.getKlass(HttpResponseImpl.class);
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
    public HttpCookie[] getCookies() {
        return cookies.toArray(HttpCookie[]::new);
    }

    public void forEachCookie(Consumer<? super HttpCookie> action) {
        for (HttpCookie cookie : cookies) {
            action.accept(cookie);
        }
    }

    @Override
    @EntityFlow
    public HttpHeader[] getHeaders() {
        return headers.toArray(HttpHeader[]::new);
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
