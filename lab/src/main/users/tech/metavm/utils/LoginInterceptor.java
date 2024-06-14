package tech.metavm.utils;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.HttpRequest;
import tech.metavm.entity.HttpResponse;
import tech.metavm.entity.Interceptor;
import tech.metavm.user.LabLoginResult;
import tech.metavm.user.LabPlatformUser;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class LoginInterceptor implements Interceptor {

    private static final String URI = "/api/" + LabPlatformUser.class.getName().replace('.', '/') + "/login";

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
