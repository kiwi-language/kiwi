package org.metavm.api.entity;

import org.metavm.api.EntityType;

import javax.annotation.Nullable;

@EntityType(systemAPI = true)
public interface HttpRequest {

    String getMethod();

    String getRequestURI();

    @Nullable String getCookie(String name);

    @Nullable String getHeader(String name);

}
