package org.manul.entity;

import org.manul.object.instance.core.Value;

public record EntityQueryField<T>(
        SearchField<T> searchField,
        EntityQueryOp op,
        Value value
) {

}
