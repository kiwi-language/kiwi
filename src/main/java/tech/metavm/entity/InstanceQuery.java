package tech.metavm.entity;

import tech.metavm.object.meta.Field;

import java.util.List;

public record InstanceQuery(
        long typeId,
        String searchText,
        List<Field> searchFields,
        boolean includeBuiltin,
        int page,
        int pageSize,
        List<InstanceQueryField> fields
) {
}
