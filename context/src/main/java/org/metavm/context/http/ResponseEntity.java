package org.metavm.context.http;

import java.util.Map;

public record ResponseEntity<T>(
        T data,
        Map<String, String> headers
) {
}
