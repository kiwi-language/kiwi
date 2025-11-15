package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityQueryBuilder<T extends Entity> {

    public static <T extends Entity> EntityQueryBuilder<T> newBuilder(Class<T> entityClass) {
        return new EntityQueryBuilder<>(entityClass);
    }

    private final Class<T> entityClass;
    private List<SearchField<? super T>> searchFields = new ArrayList<>();
    private List<EntityQueryField<? super T>> fields = new ArrayList<>();
    private boolean includeBuiltin;
    private int page = 1;
    private int pageSize = 20;
    private List<Id> newlyCreatedIds = List.of();
    private List<String> excluded = List.of();

    private EntityQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityQueryBuilder<T> fields(List<EntityQueryField<? super T>> fields) {
        this.fields = new ArrayList<>(fields);
        return this;
    }

    public EntityQueryBuilder<T> addFieldMatchIfNotNull(SearchField<? super T> field, @Nullable Value value) {
        if(value != null)
            addFieldMatch(field, value);
        return this;
    }

    public EntityQueryBuilder<T> addFieldMatch(SearchField<? super T> field, Value value) {
        this.fields.add(new EntityQueryField<>(field, EntityQueryOp.MATCH, value));
        return this;
    }

    public EntityQueryBuilder<T> addFieldNotMatch(SearchField<? super T> field, Value value) {
        this.fields.add(new EntityQueryField<>(field, EntityQueryOp.NOT_MATCH, value));
        return this;
    }

    public EntityQueryBuilder<T> addField(EntityQueryField<? super T> field) {
        this.fields.add(field);
        return this;
    }

    public EntityQueryBuilder<T> searchFields(List<SearchField<? super T>> searchFields) {
        this.searchFields = searchFields;
        return this;
    }
    public EntityQueryBuilder<T> page(int page) {
        this.page = page;
        return this;
    }
    public EntityQueryBuilder<T> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public EntityQueryBuilder<T> includeBuiltin(boolean includeBuiltin) {
        this.includeBuiltin = includeBuiltin;
        return this;
    }

    public EntityQueryBuilder<T> newlyCreated(List<Id> newlyCreated) {
        this.newlyCreatedIds = Utils.orElse(newlyCreated, List.of());
        return this;
    }

    public EntityQueryBuilder<T> excluded(List<String> excluded) {
        this.excluded = Utils.orElse(excluded, List.of());
        return this;
    }

    public EntityQuery<T> build() {
        return new EntityQuery<>(
                entityClass,
                searchFields,
                includeBuiltin,
                page,
                pageSize,
                fields,
                newlyCreatedIds,
                excluded
        );
    }

}
