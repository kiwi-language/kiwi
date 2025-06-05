package org.metavm.entity;

import org.metavm.object.instance.core.Value;
import org.metavm.util.Utils;
import org.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityQueryBuilder<T extends Entity> {

    public static <T extends Entity> EntityQueryBuilder<T> newBuilder(TypeReference<T> typeReference) {
        return newBuilder(typeReference.getType());
    }

    public static <T extends Entity> EntityQueryBuilder<T> newBuilder(Class<T> entityClass) {
        return new EntityQueryBuilder<T>(entityClass);
    }

    private final Class<T> entityClass;
    private List<SearchField<? super T>> searchFields = new ArrayList<>();
    private List<EntityQueryField<? super T>> fields = new ArrayList<>();
    private boolean includeBuiltin;
    private int page = 1;
    private int pageSize = 20;
    private List<String> newlyCreated = List.of();
    private List<String> excluded = List.of();

    private EntityQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityQueryBuilder<T> fields(List<EntityQueryField<? super T>> fields) {
        this.fields = new ArrayList<>(fields);
        return this;
    }

    public EntityQueryBuilder<T> addFieldIfNotNull(SearchField<? super T> field, @Nullable Value value) {
        if(value != null)
            addField(field, value);
        return this;
    }

    public EntityQueryBuilder<T> addField(SearchField<? super T> field, Value value) {
        this.fields.add(new EntityQueryField<>(field, value));
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

    public EntityQueryBuilder<T> newlyCreated(List<String> newlyCreated) {
        this.newlyCreated = Utils.orElse(newlyCreated, List.of());
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
                newlyCreated,
                excluded
        );
    }

}
