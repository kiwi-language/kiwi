package tech.metavm.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(2)
public class RequestTimeFilter extends OncePerRequestFilter {

    public static final Set<String> LOG_WHITE_LIST = Set.of(
            "/flow/execute",
            "/perf"
    );

    public static final Logger LOGGER = LoggerFactory.getLogger(RequestTimeFilter.class);

    public static final long LOG_PROFILE_THRESHOLD = 150L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try (var entry = ContextUtil.getProfiler().enter("Request")) {
            entry.addMessage("request", request.getMethod() + " " + request.getRequestURI());
            filterChain.doFilter(request, response);
        }
        var result = ContextUtil.getProfiler().finish(true, true);
        String requestUri = request.getRequestURI();
        if (result.duration() >= LOG_PROFILE_THRESHOLD) {
            LOGGER.info(result.output());
        }
    }
}
