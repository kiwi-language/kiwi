package org.metavm.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.metavm.util.Headers;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(4)
public class AuthenticateFilter extends OncePerRequestFilter {

    public static final Logger logger = LoggerFactory.getLogger(AuthenticateFilter.class);

    private final LoginService loginService;

    public static final Set<String> PASSING_PREFIXES = Set.of(
            "/login",
            "/login-with-token",
            "/internal-api",
            "/bootstrap",
            "/system",
            "/lab",
            "/get-login-info",
            "/register",
            "/platform-user/change-password",
            "/object",
            "/schema",
            "/api"
    );

    public AuthenticateFilter(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws IOException, ServletException {
        if (shouldPass(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        var appId = Utils.tryParseLong(request.getHeader(Headers.X_APP_ID));
        if(appId == null)
            appId = Utils.tryParseLong(request.getParameter("__app_id__"));
        if (appId != null) {
            var token = Tokens.getToken(appId, request);
            if (token != null &&  loginService.authenticate(token).isSuccessful()) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    private boolean shouldPass(HttpServletRequest request) {
        return Utils.anyMatch(PASSING_PREFIXES, p -> request.getRequestURI().startsWith(p));
    }
}
