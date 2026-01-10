package org.manul.api;

import org.manul.api.entity.HttpRequest;
import org.manul.api.entity.HttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Interceptor {

    void before(@Nonnull HttpRequest request, @Nonnull HttpResponse response);

    @Nullable Object after(@Nonnull HttpRequest request, @Nonnull HttpResponse response, @Nullable Object result);

}
