package tech.metavm.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Order(2)
public class VerificationFilter extends OncePerRequestFilter {

    @Autowired
    private LoginService loginService;

    public static final Set<String> PASSING_PREFIXES = Set.of(
            "/tenant",
            "/login",
            "/type/init-primitives",
            "/infra"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!shouldPass(request)) {
            Token token = loginService.verify(request);
            if(token != null) {
                ContextUtil.setContextInfo(token.tenantId(), token.userId());
            }
            else {
                throw BusinessException.verificationFailed();
            }
        }
        try {
            filterChain.doFilter(request, response);
        }
        finally {
            ContextUtil.clearContextInfo();
        }
    }

    private boolean shouldPass(HttpServletRequest request) {
        return NncUtils.anyMatch(PASSING_PREFIXES, p -> request.getRequestURI().startsWith(p));
    }
}
