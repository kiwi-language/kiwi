package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class InstanceQueryBuilder {

    public static InstanceQueryBuilder newBuilder(Type type) {
        return new InstanceQueryBuilder(type);
    }

    private final Type type;
    private @Nullable String searchText;
    private @Nullable String expression;
    private int page = 1;
    private int pageSize = 20;
    private boolean includeBuiltin;
    private boolean includeSubtypes = true;
    private List<Field> searchFields = List.of();
    private List<InstanceQueryField> fields = List.of();
    @NotNull
    private List<Long> newlyCreated = List.of();
    private List<Long> excluded = List.of();

    private InstanceQueryBuilder(Type type) {
        this.type = type;
    }

    public InstanceQueryBuilder page(int page) {
        this.page = page;
        return this;
    }

    public InstanceQueryBuilder searchText(@Nullable String searchText) {
        this.searchText = searchText;
        return this;
    }

    public InstanceQueryBuilder searchFields(List<Field> searchFields) {
        this.searchFields = searchFields;
        return this;
    }

    public InstanceQueryBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public InstanceQueryBuilder includeBuiltin(boolean includeBuiltin) {
        this.includeBuiltin = includeBuiltin;
        return this;
    }

    public InstanceQueryBuilder includeSubtypes(boolean includeSubtypes) {
        this.includeSubtypes = includeSubtypes;
        return this;
    }

    public InstanceQueryBuilder fields(InstanceQueryField... fields) {
        return fields(List.of(fields));
    }

    public InstanceQueryBuilder fields(List<InstanceQueryField> fields) {
        this.fields = fields;
        return this;
    }

    public InstanceQueryBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }

    public InstanceQueryBuilder newlyCreated(@Nullable List<Long> include) {
        this.newlyCreated = NncUtils.orElse(include, List.of());
        return this;
    }

    public InstanceQueryBuilder excluded(List<Long> excluded) {
        this.excluded = NncUtils.orElse(excluded, List.of());
        return this;
    }

    public InstanceQuery build() {
        return new InstanceQuery(
                type,
                searchText,
                expression,
                searchFields,
                includeBuiltin,
                includeSubtypes,
                page,
                pageSize,
                fields,
                newlyCreated,
                excluded
        );
    }


}
