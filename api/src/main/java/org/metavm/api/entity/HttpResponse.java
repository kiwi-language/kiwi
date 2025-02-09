package org.metavm.api.entity;

import javax.annotation.Nonnull;
import java.util.List;

public interface HttpResponse {

    void addCookie(String name, String value);

    void addHeader(String name, String value);

    List<HttpCookie> getCookies();

    List<HttpHeader> getHeaders();

}
