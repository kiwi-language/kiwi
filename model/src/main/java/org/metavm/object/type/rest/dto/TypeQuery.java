package org.metavm.object.type.rest.dto;

import java.util.List;

public record TypeQuery(
        String searchText,
        List<Integer> kinds,
        Boolean isTemplate,
        boolean includeAnonymous,
        boolean includeBuiltin,
        Boolean error,
        List<String> newlyCreated,
        int page,
        int pageSize
) {

}
