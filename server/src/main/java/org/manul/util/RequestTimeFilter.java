package org.manul.util;

import lombok.extern.slf4j.Slf4j;
import org.manul.server.Filter;
import org.manul.server.HttpRequest;
import org.manul.context.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
@Slf4j
public class RequestTimeFilter implements Filter {

    public static final Set<String> LOG_WHITE_LIST = Set.of(
            "/flow/execute",
            "/perf"
    );

    public static final long LOG_PROFILE_THRESHOLD = 150L;

    @Override
    public void filter(HttpRequest request, Consumer<HttpRequest> proceed) {
        try (var entry = ContextUtil.getProfiler().enter("Request")) {
            entry.addMessage("request", request.getMethod() + " " + request.getRequestPath());
            proceed.accept(request);
        }
        var result = ContextUtil.getProfiler().finish(true, true);
        if (result.duration() >= LOG_PROFILE_THRESHOLD) {
            log.info(result.output());
        }
    }

    @Override
    public int order() {
        return 2;
    }
}
