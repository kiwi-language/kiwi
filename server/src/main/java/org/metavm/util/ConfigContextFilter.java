package org.metavm.util;

import org.metavm.flow.FlowSavingContext;
import org.metavm.server.Filter;
import org.metavm.server.HttpRequest;
import org.metavm.context.Component;

import java.util.function.Consumer;

@Component
public class ConfigContextFilter implements Filter  {

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        FlowSavingContext.initConfig();
        try {
            proceed.accept(request);
        } finally {
            FlowSavingContext.clearConfig();
        }
    }

    @Override
    public int order() {
        return 1;
    }
}
