package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record QueryTypeRequest(
        String searchText,
        List<Integer> categories,
        Boolean isTemplate,
        boolean isParameterized,
        boolean includeBuiltin,
        int page,
        int pageSize
) {
}
