package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;
import tech.metavm.object.view.Mapping;

import javax.annotation.Nullable;
import java.util.List;

public record InstanceQuery(
        Klass klass,
        @Nullable String searchText,
        @Nullable String expression,
        List<Field> searchFields,
        boolean includeBuiltin,
        boolean includeSubTypes,
        int page,
        int pageSize,
        List<InstanceQueryField> fields,
        List<Id> createdIds,
        List<Id> excludedIds,
        @Nullable Mapping sourceMapping
        ) {
}
