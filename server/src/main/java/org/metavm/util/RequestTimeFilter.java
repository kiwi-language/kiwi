package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.server.Filter;
import org.metavm.server.HttpRequest;
import org.metavm.context.Component;

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
            entry.addMessage("request", request.getMethod() + " " + request.getRequestURI());
            proceed.accept(request);
        }
        var result = ContextUtil.getProfiler().finish(true, true);
        String requestUri = request.getRequestURI();
        if (result.duration() >= LOG_PROFILE_THRESHOLD) {
            log.info(result.output());
        }
    }

    @Override
    public int order() {
        return 2;
    }
}
