package org.manul.user;

import org.manul.common.ErrorCode;
import org.manul.context.Component;
import org.manul.server.Filter;
import org.manul.server.HttpRequest;
import org.manul.util.BusinessException;
import org.manul.util.Constants;
import org.manul.util.Utils;

import java.util.Set;
import java.util.function.Consumer;

@Component
public class AuthenticateFilter implements Filter  {

    private final LoginService loginService;

    public static Set<String> PREFIX_WHITELIST = Set.of(
            "/manul-system/"
    );

    public static final Set<String> PREFIX_BLACKLIST = Set.of(
            "/manul-system/ping",
            "/manul-system/auth/login",
            "/manul-system/auth/login-with-token",
            "/manul-system/auth/get-login-info",
            "/manul-system/bootstrap",
            "/manul-system/system",
            "/manul-system/lab",
            "/manul-system/register",
            "/manul-system/platform-user/change-password",
            "/manul-system/object",
            "/manul-system/schema"
    );

    public AuthenticateFilter(LoginService loginService) {
        this.loginService = loginService;
    }

    private boolean shouldPass(HttpRequest request) {
        return Utils.noneMatch(PREFIX_WHITELIST, p -> request.getRequestPath().startsWith(p)) ||
                Utils.anyMatch(PREFIX_BLACKLIST, p -> request.getRequestPath().startsWith(p));
    }

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        if (shouldPass(request)) {
            proceed.accept(request);
            return;
        }
        var token = Tokens.getToken(request);
        if (token != null &&  loginService.authenticate(new Token(Constants.PLATFORM_APP_ID, token)).isSuccessful()
                || token == null && loginService.defaultAuth()) {
            proceed.accept(request);
            return;
        }
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    @Override
    public int order() {
        return 4;
    }
}
