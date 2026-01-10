package org.manul.entity;

import org.manul.object.instance.core.Id;

public record EntityKey(
        Class<?> type,
        Id id
) {

    public static EntityKey create(Class<?> type, Id id) {
        return new EntityKey(type, id);
    }

}
