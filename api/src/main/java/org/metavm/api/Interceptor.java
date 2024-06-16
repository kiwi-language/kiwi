package org.metavm.api;

import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;

import javax.annotation.Nullable;

@EntityType(systemAPI = true)
public interface Interceptor {

    void before(HttpRequest request, HttpResponse response);

    @Nullable Object after(HttpRequest request, HttpResponse response, @Nullable Object result);

}
