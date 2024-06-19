package org.metavm.http;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.api.entity.HttpCookie;
import org.metavm.api.entity.HttpHeader;
import org.metavm.api.entity.HttpResponse;
import org.metavm.entity.Entity;
import org.metavm.entity.ReadWriteArray;

import java.util.List;

@EntityType(ephemeral = true, isNative = true)
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
