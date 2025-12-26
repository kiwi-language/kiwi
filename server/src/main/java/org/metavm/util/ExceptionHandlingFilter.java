package org.metavm.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsonk.Jsonk;
import org.metavm.common.ErrorCode;
import org.metavm.common.ErrorResponse;
import org.metavm.server.Filter;
import org.metavm.server.HttpRequest;
import org.metavm.context.Component;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
@Slf4j
public class ExceptionHandlingFilter implements Filter {

    private BusinessException extractBusinessException(Throwable e) {
        while (e != null) {
            if(e instanceof BusinessException businessException) {
                return businessException;
            }
            e = e.getCause();
        }
        return null;
    }

    @SneakyThrows
    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        try {
            proceed.accept(request);
        }
        catch (Exception e) {
            BusinessException bizExp = extractBusinessException(e);
            if(bizExp != null) {
                var failureResult = ErrorResponse.create(bizExp.getErrorCode(), bizExp.getParams());
                if(bizExp.getErrorCode() == ErrorCode.VERIFICATION_FAILED)
                    request.setStatus(401);
                else
                    request.setStatus(400);
                request.addHeader("content-type","application/json;charset=UTF-8");
//                request.setCharacterEncoding("UTF-8");
                request.getOut().write(Jsonk.toJson(failureResult).getBytes(StandardCharsets.UTF_8));
                log.info("business exception", bizExp);
            }
            else {
                throw e;
            }
        }
    }

    @Override
    public int order() {
        return 1;
    }
}
