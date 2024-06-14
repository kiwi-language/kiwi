package org.metavm.object.type.rest.dto;

import java.util.List;

public record GetTypesRequest(
        List<String> ids,
        boolean includingPropertyTypes
) {
}
