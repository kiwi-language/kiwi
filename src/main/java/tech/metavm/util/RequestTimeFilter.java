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

@Component
@Order(2)
public class RequestTimeFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(RequestTimeFilter.class);

    public static final long LOG_PROFILE_THRESHOLD = 0L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try(var entry = ContextUtil.getProfiler().enter("Request")) {
            entry.addMessage("uri", request.getRequestURI());
            filterChain.doFilter(request, response);
        }
        var result = ContextUtil.getProfiler().finish();
        if(result.duration() > LOG_PROFILE_THRESHOLD) {
            LOGGER.info(result.output());
        }
    }
}
