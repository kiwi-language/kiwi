package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;

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
        List<Id> excludedIds
        ) {
}
