package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record BatchGetRequest (
        List<Long> ids,
        boolean includingFields,
        boolean includingFieldTypes
) {
}
