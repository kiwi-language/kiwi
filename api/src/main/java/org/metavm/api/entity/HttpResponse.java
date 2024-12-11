package org.metavm.api.entity;

import org.metavm.api.Entity;

import javax.annotation.Nonnull;
import java.util.List;

@Entity(systemAPI = true)
public interface HttpResponse {

    void addCookie(String name, String value);

    void addHeader(String name, String value);

    @Nonnull
    List<HttpCookie> getCookies();

    @Nonnull List<HttpHeader> getHeaders();

}
