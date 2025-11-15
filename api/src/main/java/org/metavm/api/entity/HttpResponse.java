package org.metavm.api.entity;

public interface HttpResponse {

    void addCookie(String name, String value);

    void addHeader(String name, String value);

    HttpCookie[] getCookies();

    HttpHeader[] getHeaders();

}
