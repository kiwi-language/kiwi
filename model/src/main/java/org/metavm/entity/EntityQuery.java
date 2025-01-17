package org.metavm.entity;

import java.util.List;

public record EntityQuery<T extends Entity>(
        Class<T> entityType,
        List<SearchField<? super T>> searchFields,
        boolean includeBuiltin,
        int page,
        int pageSize,
        List<EntityQueryField<? super T>> fields,
        List<String> newlyCreated,
        List<String> excluded
) {

}
