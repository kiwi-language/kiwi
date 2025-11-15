package org.metavm.server;

import java.util.function.Consumer;

public record RouteConfig(
        HttpMethod method,
        String path,
        Consumer<HttpRequest> handle
) {
}
