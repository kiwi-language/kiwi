package org.metavm.server;

import java.util.List;
import java.util.Map;

public record MatchedRoute(
    Map<String, String> pathVariables,
    Map<String, List<String>> queryParameters
) {
}
