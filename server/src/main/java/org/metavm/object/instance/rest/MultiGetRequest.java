package org.metavm.object.instance.rest;

import org.jsonk.Json;

import java.util.List;

@Json
public record MultiGetRequest(
        long appId,
        List<String> ids,
        boolean excludeChildren,
        boolean excludeFields
) {
}
