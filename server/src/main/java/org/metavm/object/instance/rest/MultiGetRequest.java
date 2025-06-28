package org.metavm.object.instance.rest;

import java.util.List;

public record MultiGetRequest(
        List<String> ids,
        boolean excludeChildren,
        boolean excludeFields
) {
}
