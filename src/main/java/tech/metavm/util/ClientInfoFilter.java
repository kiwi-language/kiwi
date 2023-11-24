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
public class ClientInfoFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClientInfoFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContextUtil.setClientId(request.getHeader("ClientId"));
        var metaVersionHeader = request.getHeader("MetaVersion");
        Long metaVersion = NncUtils.tryParseLong(metaVersionHeader);
        if(metaVersion != null)
            ContextUtil.setMetaVersion(metaVersion);
        filterChain.doFilter(request, response);
    }

}
