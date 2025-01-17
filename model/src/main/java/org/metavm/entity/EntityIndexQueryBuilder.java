package org.metavm.entity;

public class EntityIndexQueryBuilder<T extends Entity> {

    public static <T extends Entity> EntityIndexQueryBuilder<T> newBuilder(IndexDef<T> indexDef) {
        return new EntityIndexQueryBuilder<>(indexDef);
    }

    private final IndexDef<T> indexDef;
    private EntityIndexKey from;
    private EntityIndexKey to;
    private boolean desc;
    private long limit = 1000;

    private EntityIndexQueryBuilder(IndexDef<T> indexDef) {
        this.indexDef = indexDef;
    }

    public EntityIndexQueryBuilder<T> from(EntityIndexKey from) {
        this.from = from;
        return this;
    }

    public EntityIndexQueryBuilder<T> to(EntityIndexKey to) {
        this.to = to;
        return this;
    }

    public EntityIndexQueryBuilder<T> eq(EntityIndexKey key) {
        this.from = key;
        this.to = key;
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
                from,
                to,
                desc,
                limit
        );
    }

}
