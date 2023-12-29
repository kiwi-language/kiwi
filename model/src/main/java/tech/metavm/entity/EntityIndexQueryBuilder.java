package tech.metavm.entity;

import java.util.ArrayList;
import java.util.List;

public class EntityIndexQueryBuilder<T> {

    public static <T> EntityIndexQueryBuilder<T> newBuilder(IndexDef<T> indexDef) {
        return new EntityIndexQueryBuilder<>(indexDef);
    }

    private final IndexDef<T> indexDef;
    private List<EntityIndexQueryItem> items = new ArrayList<>();
    private boolean desc;
    private long limit = 1000;

    private EntityIndexQueryBuilder(IndexDef<T> indexDef) {
        this.indexDef = indexDef;
    }

    public EntityIndexQueryBuilder<T> items(List<EntityIndexQueryItem> items) {
        this.items = new ArrayList<>(items);
        return this;
    }

    public EntityIndexQueryBuilder<T> addEqItem(String fieldName, Object value) {
        items.add(new EntityIndexQueryItem(fieldName, IndexOperator.EQ, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> addGtItem(String fieldName, Object value) {
        items.add(new EntityIndexQueryItem(fieldName, IndexOperator.GT, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> addEqItem(int fieldIndex, Object value) {
        items.add(new EntityIndexQueryItem(indexDef.getFieldName(fieldIndex), IndexOperator.EQ, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> addGeItem(int fieldIndex, Object value) {
        items.add(new EntityIndexQueryItem(indexDef.getFieldName(fieldIndex), IndexOperator.GE, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> addGtItem(int fieldIndex, Object value) {
        items.add(new EntityIndexQueryItem(indexDef.getFieldName(fieldIndex), IndexOperator.GT, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> addLeItem(int fieldIndex, Object value) {
        items.add(new EntityIndexQueryItem(indexDef.getFieldName(fieldIndex), IndexOperator.LE, value));
        return this;
    }

    public EntityIndexQueryBuilder<T> desc(boolean desc) {
        this.desc = desc;
        return this;
    }

    public EntityIndexQueryBuilder<T> limit(long limit) {
        this.limit = limit;
        return this;
    }

    public EntityIndexQuery<T> build() {
        return new EntityIndexQuery<>(
                indexDef,
                items,
                desc,
                limit
        );
    }

}
