package org.metavm.object.instance.rest;

import java.util.List;

public record SearchResult(
        List<Object> page,
        long total
) {
}
