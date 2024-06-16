package org.metavm.api.entity;

import org.metavm.api.EntityType;

import java.util.List;

@EntityType(systemAPI = true)
public interface HttpResponse {

    void addCookie(String name, String value);

    void addHeader(String name, String value);

    List<HttpCookie> getCookies();

    List<HttpHeader> getHeaders();

}
