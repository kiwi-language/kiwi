package org.metavm.util;

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

import java.io.IOException;

@Component
@Order(3)
public class ClientInfoFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClientInfoFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        ContextUtil.setClientId(request.getHeader(Headers.CLIENT_ID));
        Long metaVersion = NncUtils.tryParseLong(request.getHeader(Headers.META_VERSION));
        if(metaVersion != null)
            ContextUtil.setMetaVersion(metaVersion);
        filterChain.doFilter(request, response);
    }

}
