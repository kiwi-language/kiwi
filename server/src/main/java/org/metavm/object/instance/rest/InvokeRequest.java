package org.metavm.object.instance.rest;

import org.jsonk.Json;

import java.util.Map;

@Json
public record InvokeRequest(
        long appId,
        Object receiver,
        String method,
        Map<String, Object> arguments
) {
}
