package org.manul.util;

import lombok.extern.slf4j.Slf4j;
import org.manul.server.Filter;
import org.manul.server.HttpRequest;
import org.manul.context.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class ContextInitFilter implements Filter {

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        try {
            ContextUtil.initContextInfo();
            proceed.accept(request);
        }
        finally {
            ContextUtil.clearContextInfo();
        }
    }

    @Override
    public int order() {
        return 1;
    }
}
