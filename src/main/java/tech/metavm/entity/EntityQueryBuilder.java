package tech.metavm.entity;

import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;

public class EntityQueryBuilder<T extends Entity> {

    public static <T extends Entity> EntityQueryBuilder<T> newBuilder(TypeReference<T> typeReference) {
        return newBuilder(typeReference.getType());
    }

    public static <T extends Entity> EntityQueryBuilder<T> newBuilder(Class<T> entityClass) {
        return new EntityQueryBuilder<T>(entityClass);
    }

    private final Class<T> entityClass;
    private String searchText;
    private @Nullable String expression;
    private List<String> searchFields = List.of();
    private List<EntityQueryField> fields = List.of();
    private boolean includeBuiltin;
    private int page = 1;
    private int pageSize = 20;
    private List<Long> newlyCreated = List.of();

    private EntityQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityQueryBuilder<T> searchText(String searchText) {
        this.searchText = searchText;
        return this;
    }

    public EntityQueryBuilder<T> fields(EntityQueryField...fields) {
        return fields(List.of(fields));
    }

    public EntityQueryBuilder<T> fields(List<EntityQueryField> fields) {
        this.fields = fields;
        return this;
    }
    public EntityQueryBuilder<T> searchFields(List<String> searchFields) {
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

    public EntityQueryBuilder<T> expression(@Nullable String expression) {
        this.expression = expression;
        return this;
    }

    public EntityQueryBuilder<T> includeBuiltin(boolean includeBuiltin) {
        this.includeBuiltin = includeBuiltin;
        return this;
    }

    public EntityQueryBuilder<T> newlyCreated(List<Long> newlyCreated) {
        this.newlyCreated = NncUtils.orElse(newlyCreated, List.of());
        return this;
    }

    public EntityQuery<T> build() {
        return new EntityQuery<T>(
                entityClass,
                searchText,
                expression,
                searchFields,
                includeBuiltin,
                page,
                pageSize,
                fields,
                newlyCreated
        );
    }

}
