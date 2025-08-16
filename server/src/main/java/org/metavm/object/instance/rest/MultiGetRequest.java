package org.metavm.object.instance.rest;

import java.util.List;

public record MultiGetRequest(
        long appId,
        List<String> ids,
        boolean excludeChildren,
        boolean excludeFields
) {
}
