package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Index;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class IndexDef<T extends Entity> {

    public static <T extends Entity> IndexDef<T> create(Class<T> klass, int fieldCount, Function<T, List<Value>> valuesFunc) {
        return new IndexDef<>(klass, klass, fieldCount, false, valuesFunc);
    }

    public static <T extends Entity> IndexDef<T> createUnique(Class<T> klass, int fieldCount, Function<T, List<Value>> valuesFunc) {
        return new IndexDef<>(klass, klass, fieldCount, true, valuesFunc);
    }

    private final Class<T> type;
    private final Type genericType;
    private final int fieldCount;
    private final Function<T, List<Value>> valuesFunc;
    private final boolean unique;
    private Index index;

    private IndexDef(Class<T> type, Type genericType, int fieldCount, boolean unique, Function<T, List<Value>> valuesFunc) {
        this.type = type;
        this.genericType = genericType;
        this.fieldCount = fieldCount;
        this.unique = unique;
        this.valuesFunc = valuesFunc;
    }

    public Class<T> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Function<T, List<Value>> getValuesFunc() {
        return valuesFunc;
    }

    public boolean isUnique() {
        return unique;
    }

    public EntityIndexQueryBuilder<T> newQueryBuilder() {
        return EntityIndexQueryBuilder.newBuilder(this);
    }


    public @NotNull List<Value> getValues(org.metavm.entity.Entity entity) {
        return Objects.requireNonNull(valuesFunc.apply(type.cast(entity)),
                () -> "Failed to get index key values for " + type.getName() + "." + index
        );
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }
}
