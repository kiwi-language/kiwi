package org.metavm.api.entity;

import org.metavm.api.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(systemAPI = true)
public interface HttpRequest {

    @Nonnull
    String getMethod();

    @Nonnull
    String getRequestURI();

    @Nullable String getCookie(String name);

    @Nullable String getHeader(String name);

}
