package org.metavm.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Index;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.*;
import org.metavm.http.HttpCookieImpl;
import org.metavm.http.HttpHeaderImpl;
import org.metavm.http.HttpRequestImpl;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

@Slf4j
public enum StdKlass {

    string(String.class),
    enum_(java.lang.Enum.class),
    httpRequestImpl(HttpRequestImpl.class),
    httpResponseImpl(HttpResponseImpl.class),
    exception(Exception.class),
    index(Index.class),
    httpCookieImpl(HttpCookieImpl.class),
    httpHeaderImpl(HttpHeaderImpl.class),
    interceptor(Interceptor.class),
    httpRequest(HttpRequest.class),
    httpResponse(HttpResponse.class),
    httpCookie(HttpCookie.class),
    httpHeader(HttpHeader.class),

    ;

    @Getter
    private final Class<?> javaClass;
    private final Klass klass;

    StdKlass(Class<?> javaClass) {
        this.javaClass = javaClass;
        this.klass = StdKlassRegistry.instance.getKlass(javaClass);
    }

    public Klass get() {
        return klass;
    }

    public ClassType type() {
        return get().getType();
    }

}
