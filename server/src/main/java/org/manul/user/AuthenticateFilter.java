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

    public static final Set<String> PASSING_PREFIXES = Set.of(
            "/ping",
            "/auth/login",
            "/auth/login-with-token",
            "/auth/get-login-info",
            "/internal-api",
            "/bootstrap",
            "/system",
            "/lab",
            "/register",
            "/platform-user/change-password",
            "/object",
            "/schema",
            "/api"
    );

    public AuthenticateFilter(LoginService loginService) {
        this.loginService = loginService;
    }

    private boolean shouldPass(HttpRequest request) {
        return Utils.anyMatch(PASSING_PREFIXES, p -> request.getRequestURI().startsWith(p));
    }

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        if (shouldPass(request)) {
            proceed.accept(request);
            return;
        }
        var token = Tokens.getToken(request);
        if (token != null &&  loginService.authenticate(new Token(Constants.PLATFORM_APP_ID, token)).isSuccessful()
                || loginService.defaultAuth()) {
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
