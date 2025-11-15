package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.server.Filter;
import org.metavm.server.HttpRequest;
import org.metavm.context.Component;

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
