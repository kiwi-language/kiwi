package tech.metavm.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.io.IOException;
import java.util.Set;

@Component
@Order(3)
public class VerificationFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(VerificationFilter.class);

    @Autowired
    private LoginService loginService;

    public static final Set<String> PASSING_PREFIXES = Set.of(
            "/tenant",
            "/login",
            "/bootstrap",
            "/management",
            "/lab",
            "/get-tenant-id"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!shouldPass(request)) {
            var result = loginService.verify(Tokens.getToken(request));
            ContextUtil.setLoginInfo(result.tenantId(), result.userId());
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldPass(HttpServletRequest request) {
        return NncUtils.anyMatch(PASSING_PREFIXES, p -> request.getRequestURI().startsWith(p));
    }
}
