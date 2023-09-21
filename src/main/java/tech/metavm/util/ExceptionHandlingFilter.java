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
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
                if(bizExp.getErrorCode() == ErrorCode.VERIFICATION_FAILED) {
                    response.setStatus(401);
                }
                response.setHeader("content-type","application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(NncUtils.toJSONString(failureResult).getBytes(StandardCharsets.UTF_8));
                LOGGER.info("business exception", bizExp);
            }
            else {
                throw e;
            }
        }
    }

    private BusinessException extractBusinessException(Throwable e) {
        while (e != null) {
            if(e instanceof BusinessException businessException) {
                return businessException;
            }
            e = e.getCause();
        }
        return null;
    }

}
