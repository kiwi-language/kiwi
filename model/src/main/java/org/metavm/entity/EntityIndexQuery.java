package org.metavm.entity;

import javax.annotation.Nullable;

public record EntityIndexQuery<T extends Entity>(
        IndexDef<T> indexDef,
        @Nullable EntityIndexKey from,
        @Nullable EntityIndexKey to,
        boolean desc,
        long limit
) {

}
