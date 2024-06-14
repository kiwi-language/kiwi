package org.metavm.entity;

import javax.annotation.Nullable;

public interface HttpRequest {

    String getMethod();

    String getRequestURI();

    @Nullable String getCookie(String name);

    @Nullable String getHeader(String name);

}
