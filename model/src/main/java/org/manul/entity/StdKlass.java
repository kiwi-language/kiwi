package org.manul.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.manul.api.Index;
import org.manul.api.Interceptor;
import org.manul.api.entity.*;
import org.manul.http.HttpCookieImpl;
import org.manul.http.HttpHeaderImpl;
import org.manul.http.HttpRequestImpl;
import org.manul.http.HttpResponseImpl;
import org.manul.object.type.ClassType;
import org.manul.object.type.Klass;

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
