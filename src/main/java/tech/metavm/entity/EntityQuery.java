package tech.metavm.entity;

import tech.metavm.util.TypeReference;

import java.util.List;

public record EntityQuery<T extends Entity>(
        Class<T> entityType,
        String searchText,
        List<String> searchFields,
        int page,
        int pageSize,
        List<EntityQueryField> fields
) {

    public static <T extends Entity> EntityQuery<T> create(
            Class<T> entityType,
            String searchText,
            int page,
            int pageSize
    ) {
        return new EntityQuery<>(
                entityType,
                searchText,
                List.of(),
                page,
                pageSize,
                List.of()
        );
    }

    public static <T extends Entity> EntityQuery<T> create(
            Class<T> entityType,
            String searchText,
            int page,
            int pageSize,
            List<EntityQueryField> fields
    ) {
        return new EntityQuery<>(
                entityType,
                searchText,
                List.of(),
                page,
                pageSize,
                fields
        );
    }

    public static <T extends Entity> EntityQuery<T> create(
            TypeReference<T> typeReference,
            String searchText,
            int page,
            int pageSize,
            List<EntityQueryField> fields
    ) {
        return new EntityQuery<>(
                typeReference.getType(),
                searchText,
                List.of(),
                page,
                pageSize,
                fields
        );
    }

}
