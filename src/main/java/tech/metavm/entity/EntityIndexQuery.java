package tech.metavm.entity;

import java.util.List;

public record EntityIndexQuery<T>(
        IndexDef<T> indexDef,
        List<EntityIndexQueryItem> items,
        IndexQueryOperator lastOperator,
        boolean desc,
        long limit
) {

    public static <T> EntityIndexQuery<T> create(IndexDef<T> indexDef, List<EntityIndexQueryItem> items) {
        return create(indexDef, items, IndexQueryOperator.EQ);
    }

    public static <T> EntityIndexQuery<T> create(IndexDef<T> indexDef, List<EntityIndexQueryItem> items, IndexQueryOperator lastOperator) {
        return create(indexDef, items, lastOperator, -1L);
    }

    public static <T> EntityIndexQuery<T> create(IndexDef<T> indexDef, List<EntityIndexQueryItem> items, IndexQueryOperator lastOperator, long limit) {
        return new EntityIndexQuery<>(indexDef, items, lastOperator, false, limit);
    }


}
