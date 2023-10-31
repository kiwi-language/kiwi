package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record GetTypesRequest(
        List<Long> ids,
        boolean includingPropertyTypes
) {
}
