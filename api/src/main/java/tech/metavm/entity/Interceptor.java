package tech.metavm.entity;

import javax.annotation.Nullable;

public interface Interceptor {

    void before(HttpRequest request, HttpResponse response);

    @Nullable Object after(HttpRequest request, HttpResponse response, @Nullable Object result);

}
