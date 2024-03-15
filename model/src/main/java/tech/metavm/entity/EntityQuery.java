package tech.metavm.entity;

import java.util.List;

public record EntityQuery<T extends Entity>(
        Class<T> entityType,
        String searchText,
        String expression,
        List<String> searchFields,
        boolean includeBuiltin,
        int page,
        int pageSize,
        List<EntityQueryField> fields,
        List<String> newlyCreated,
        List<String> excluded
) {

}
