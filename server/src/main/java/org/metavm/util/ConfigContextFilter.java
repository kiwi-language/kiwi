package org.metavm.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.metavm.flow.FlowSavingContext;

import java.io.IOException;

@Component
@Order(1)
public class ConfigContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        FlowSavingContext.initConfig();
        try {
            filterChain.doFilter(request, response);
        } finally {
            FlowSavingContext.clearConfig();
        }
    }
}
