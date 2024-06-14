package org.metavm.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.metavm.util.Headers;
import org.metavm.util.NncUtils;

import java.io.IOException;
import java.util.Set;

@Component
@Order(4)
public class VerificationFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(VerificationFilter.class);

    private final LoginService loginService;

    public static final Set<String> PASSING_PREFIXES = Set.of(
            "/login",
            "/bootstrap",
            "/system",
            "/lab",
            "/get-login-info",
            "/register",
            "/platform-user/change-password",
            "/api"
    );

    public VerificationFilter(LoginService loginService) {
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
        var appId = NncUtils.tryParseLong(request.getHeader(Headers.APP_ID));
        if(appId == null)
            appId = NncUtils.tryParseLong(request.getParameter("__app_id__"));
        if (appId != null) {
            var token = Tokens.getToken(appId, request);
            if (token != null &&  loginService.verify(token).isSuccessful()) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    private boolean shouldPass(HttpServletRequest request) {
        return NncUtils.anyMatch(PASSING_PREFIXES, p -> request.getRequestURI().startsWith(p));
    }
}
