package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record QueryTypeRequest(
        String searchText,
        List<Integer> categories,
        Boolean isTemplate,
        boolean includeAnonymous,
        boolean includeBuiltin,
        Boolean error,
        int page,
        int pageSize
) {
}
