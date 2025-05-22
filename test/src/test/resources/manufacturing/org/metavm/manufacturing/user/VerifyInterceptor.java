package org.metavm.manufacturing.user;

import org.metavm.api.Component;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.utils.ContextKeys;
import org.metavm.manufacturing.utils.CookieNames;

import javax.annotation.Nullable;
import java.util.Set;

@Component
public class VerifyInterceptor implements Interceptor {
    public static final Set<String> whiteList = Set.of(
            "/object/user-service/signup",
            "/object/user-service/login"
    );

    private final UserService userService;

    public VerifyInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void before(HttpRequest request, HttpResponse response) {
        // TODO auth process temporarily removed caused by object API path refactoring
//        if(!whiteList.contains(request.getRequestURI())) {
            var token = request.getCookie(CookieNames.TOKEN);
            if(token == null)
                return;
//                throw new IllegalStateException("Auth failed");
            var user = userService.verify(token);
            if(user == null)
                return;
//                throw new IllegalStateException("Auth failed");
            Lang.setContext(ContextKeys.USER, user);
//        }
    }

    @Nullable
    @Override
    public Object after(HttpRequest request, HttpResponse response, @Nullable Object result) {
        return result;
    }
}
