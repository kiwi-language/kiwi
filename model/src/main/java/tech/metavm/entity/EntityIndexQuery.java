package tech.metavm.entity;

import javax.annotation.Nullable;

public record EntityIndexQuery<T>(
        IndexDef<T> indexDef,
        @Nullable EntityIndexKey from,
        @Nullable EntityIndexKey to,
        boolean desc,
        long limit
) {

}
