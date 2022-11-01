package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.metavm.dto.Result;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        }
        catch (Exception e) {
            BusinessException bizExp = extractBusinessException(e);
            if(bizExp != null) {
                Result<?> failureResult = Result.failure(bizExp.getErrorCode(), bizExp.getParams());
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(NncUtils.toJSONString(failureResult));
                LOGGER.info("business exception", bizExp);
            }
            else {
                throw e;
            }
        }
    }

    private BusinessException extractBusinessException(Exception e) {
        if(e instanceof BusinessException bizExp) {
            return bizExp;
        }
        if(e.getCause() instanceof BusinessException bizExp) {
            return bizExp;
        }
        return null;
    }

}
