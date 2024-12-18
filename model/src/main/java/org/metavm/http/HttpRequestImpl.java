package org.metavm.http;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpRequest;
import org.metavm.entity.ValueArray;
import org.metavm.util.NncUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Entity(systemAPI = true)
public class HttpRequestImpl extends org.metavm.entity.Entity implements HttpRequest, ValueObject {

    private final String method;
    private final String requestURI;
    private final ValueArray<HttpCookie> cookies;
    private final ValueArray<HttpHeader> headers;

    public HttpRequestImpl(String method, String requestURI, List<HttpHeader> headers, List<HttpCookie> cookies) {
        this.method = method;
        this.requestURI = requestURI;
        this.headers = new ValueArray<>(HttpHeader.class, headers);
        this.cookies = new ValueArray<>(HttpCookie.class, cookies);
    }

    @Override
    @Nonnull
    public String getMethod() {
        return method;
    }

    @Override
    @Nonnull
    public String getRequestURI() {
        return requestURI;
    }

    @Nullable
    @Override
    public String getCookie(String name) {
        return NncUtils.findAndMap(cookies, c -> c.name().equals(name), HttpCookie::value);
    }

    @Nullable
    @Override
    public String getHeader(String name) {
        return NncUtils.findAndMap(headers, h -> h.name().equals(name), HttpHeader::value);
    }
}
