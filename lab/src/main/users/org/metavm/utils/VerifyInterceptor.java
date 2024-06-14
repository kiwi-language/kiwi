package org.metavm.utils;

import org.metavm.entity.*;
import org.metavm.user.LabSession;

import javax.annotation.Nullable;

@EntityType
public class VerifyInterceptor implements Interceptor {
    @Override
    public void before(HttpRequest request, HttpResponse response) {
        var token = request.getCookie("token");
        if(token == null)
            throw new IllegalStateException("Login required");
        var session = IndexUtils.selectFirst(new LabSession.TokenIndex(token));
        if(session == null)
            throw new IllegalStateException("Login required");
        RequestUtils.setContext("user", session.getUser());
    }

    @Override
    public @Nullable Object after(HttpRequest request, HttpResponse response, @Nullable Object result) {
        return result;
    }
}
