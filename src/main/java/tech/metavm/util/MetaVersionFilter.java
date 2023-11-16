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
@Order(4)
public class MetaVersionFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(MetaVersionFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String metaVersionStr = request.getHeader("MetaVersion");
        if(metaVersionStr != null) {
            try {
                var metaVersion = Long.parseLong(metaVersionStr);
                ContextUtil.setMetaVersion(metaVersion);
            }
            catch (NumberFormatException e) {
                LOGGER.error("Malformed meta version {}", metaVersionStr);
            }
        }
        filterChain.doFilter(request, response);
    }

}
