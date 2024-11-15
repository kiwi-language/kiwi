package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class InstanceQueryBuilder {

    public static InstanceQueryBuilder newBuilder(Klass klass) {
        return new InstanceQueryBuilder(klass);
    }

    private final Klass klass;
    @Nullable
    private String searchText;
    @Nullable
    private String expression;
    private int page = 1;
    private int pageSize = 20;
    private boolean includeBuiltin;
    private boolean includeSubtypes = true;
    private List<Field> searchFields = List.of();
    private List<InstanceQueryField> fields = List.of();
    @NotNull
    private List<Id> createdIds = List.of();
    private List<Id> excludedIds = List.of();

    private InstanceQueryBuilder(Klass klass) {
        this.klass = klass;
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

    public InstanceQueryBuilder newlyCreated(@Nullable List<Id> newlyCreated) {
        this.createdIds = NncUtils.orElse(newlyCreated, List.of());
        return this;
    }

    public InstanceQueryBuilder excluded(List<Id> excluded) {
        this.excludedIds = NncUtils.orElse(excluded, List.of());
        return this;
    }

    public InstanceQuery build() {
        return new InstanceQuery(
                klass,
                searchText,
                expression,
                searchFields,
                includeBuiltin,
                includeSubtypes,
                page,
                pageSize,
                fields,
                createdIds,
                excludedIds
        );
    }


}
