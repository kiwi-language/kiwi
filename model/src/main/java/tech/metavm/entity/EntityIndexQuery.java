package tech.metavm.entity;

import java.util.List;

public record EntityIndexQuery<T>(
        IndexDef<T> indexDef,
        List<EntityIndexQueryItem> items,
        IndexOperator lastOperator,
        boolean desc,
        long limit
) {

}
