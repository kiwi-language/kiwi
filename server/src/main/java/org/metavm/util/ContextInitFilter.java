package org.metavm.util;

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
@Order(1)
public class ContextInitFilter extends OncePerRequestFilter {

    public static final Logger logger = LoggerFactory.getLogger(ContextInitFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            ContextUtil.initContextInfo();
            filterChain.doFilter(request, response);
        }
        finally {
            ContextUtil.clearContextInfo();
        }
    }

}
