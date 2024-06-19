package org.metavm.manufacturing.user;

import org.metavm.api.Component;
import org.metavm.api.Interceptor;
import org.metavm.api.entity.HttpRequest;
import org.metavm.api.entity.HttpResponse;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.utils.ContextKeys;
import org.metavm.manufacturing.utils.CookieNames;

import javax.annotation.Nullable;

@Component
public class SetTokenInterceptor implements Interceptor {

    @Override
    public void before(HttpRequest request, HttpResponse response) {

    }

    @Nullable
    @Override
    public Object after(HttpRequest request, HttpResponse response, @Nullable Object result) {
        var token = (String) Lang.getContext(ContextKeys.TOKEN);
        if(token != null)
            response.addCookie(CookieNames.TOKEN, token);
        return result;
    }
}
