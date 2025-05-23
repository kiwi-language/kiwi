package org.metavm.entity;

import org.metavm.object.instance.core.Id;

public record EntityKey(
        Class<?> type,
        Id id
) {

    public static EntityKey create(Class<?> type, Id id) {
        return new EntityKey(EntityUtils.getEntityType(type), id);
    }

}
