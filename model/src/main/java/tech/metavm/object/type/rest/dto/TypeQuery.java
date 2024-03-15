package tech.metavm.object.type.rest.dto;

import java.util.List;

public record TypeQuery(
        String searchText,
        List<Integer> categories,
        Boolean isTemplate,
        boolean includeAnonymous,
        boolean includeBuiltin,
        Boolean error,
        List<String> newlyCreated,
        int page,
        int pageSize
) {

}
