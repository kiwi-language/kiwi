package org.manul.util;

import org.manul.flow.FlowSavingContext;
import org.manul.server.Filter;
import org.manul.server.HttpRequest;
import org.manul.context.Component;

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
