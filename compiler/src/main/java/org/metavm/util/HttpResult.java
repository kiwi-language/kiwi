package org.metavm.util;

import java.util.List;
import java.util.Map;

public record HttpResult<R>(R data, Map<String, List<String>> headers) {
}
