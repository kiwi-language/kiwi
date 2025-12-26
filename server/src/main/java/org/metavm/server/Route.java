package org.metavm.server;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class Route {

    public static RouteConfig create(HttpMethod method, String path,
                                     Consumer<HttpRequest> handle) {
        return new RouteConfig(method, path, handle);
    }

    public static RouteConfig createPost(String path,
                                         Consumer<HttpRequest> handle) {
        return new RouteConfig(
                HttpMethod.POST,
                path,
                handle
        );
    }

    private final HttpMethod method;
    private final PathPattern pathPattern;
    private final Consumer<HttpRequest> handle;

    public Route(HttpMethod method, String pathPattern, Consumer<HttpRequest> handle) {
        this.method = method;
        this.pathPattern = new PathPattern(pathPattern);
        this.handle = handle;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public @Nullable MatchedRoute match(String path, String query) {
        var pathVars = this.pathPattern.match(path);
        if (pathVars != null)
            return new MatchedRoute(pathVars, Queries.parseQuery(query));
        else
            return null;
    }

    public void process(HttpRequest request) {
        handle.accept(request);
    }
}