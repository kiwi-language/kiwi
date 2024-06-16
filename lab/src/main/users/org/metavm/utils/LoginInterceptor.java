package org.metavm.utils;

import org.metavm.api.EntityType;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.api.Interceptor;
import org.metavm.user.LabLoginResult;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class LoginInterceptor implements Interceptor {

    private static final String URI = "/api/org/metavm/user/LabPlatformUser/login";

    @Override
    public void before(HttpRequest request, HttpResponse response) {
    }

    @Override
    public @Nullable Object after(HttpRequest request, HttpResponse response, @Nullable Object result) {
        if(request.getRequestURI().equals(URI)) {
            var loginResult = (LabLoginResult) Objects.requireNonNull(result);
            response.addCookie("token", loginResult.token());
            return loginResult;
        }
        return result;
    }

}
