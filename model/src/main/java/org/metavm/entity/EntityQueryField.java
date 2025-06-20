package org.metavm.entity;

import org.metavm.object.instance.core.Value;

public record EntityQueryField<T>(
        SearchField<T> searchField,
        EntityQueryOp op,
        Value value
) {

}
