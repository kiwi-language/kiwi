package tech.metavm.http;

import tech.metavm.entity.*;

import java.util.List;

@EntityType(ephemeral = true)
public class HttpResponseImpl extends Entity implements HttpResponse {

    @ChildEntity
    private final ReadWriteArray<HttpHeader> headers = addChild(new ReadWriteArray<>(HttpHeader.class), "headers");

    @ChildEntity
    private final ReadWriteArray<HttpCookie> cookies = addChild(new ReadWriteArray<>(HttpCookie.class), "cookies");

    @Override
    public void addCookie(String name, String value) {
        cookies.removeIf(c -> c.name().equals(name));
        cookies.add(new HttpCookie(name, value));
    }

    @Override
    public void addHeader(String name, String value) {
        headers.removeIf(h -> h.name().equals(name));
        headers.add(new HttpHeader(name, value));
    }

    @Override
    public List<HttpCookie> getCookies() {
        return cookies.toList();
    }

    @Override
    public List<HttpHeader> getHeaders() {
        return headers.toList();
    }
}
