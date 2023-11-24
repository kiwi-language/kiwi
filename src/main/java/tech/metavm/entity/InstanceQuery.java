package tech.metavm.entity;

import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.List;

public record InstanceQuery(
        Type type,
        @Nullable String searchText,
        @Nullable String expression,
        List<Field> searchFields,
        boolean includeBuiltin,
        boolean includeSubTypes,
        int page,
        int pageSize,
        List<InstanceQueryField> fields,
        List<Long> newlyCreated
) {
}
