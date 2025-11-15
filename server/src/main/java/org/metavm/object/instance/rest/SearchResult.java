package org.metavm.object.instance.rest;

import org.jsonk.Json;

import java.util.List;

@Json
public record SearchResult(
        List<Object> items,
        long total
) {
}
