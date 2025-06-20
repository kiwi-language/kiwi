package org.metavm.api;

import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Interceptor {

    void before(@Nonnull HttpRequest request, @Nonnull HttpResponse response);

    @Nullable Object after(@Nonnull HttpRequest request, @Nonnull HttpResponse response, @Nullable Object result);

}
