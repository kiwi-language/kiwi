package org.metavm.object.instance.rest;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record InvokeRequest(
        long appId,
        @NotNull
        Object receiver,
        @NotNull
        String method,
        @NotNull
        Map<String, Object> arguments
) {
}
