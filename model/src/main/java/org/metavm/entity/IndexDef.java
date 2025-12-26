package org.metavm.entity;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Index;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class IndexDef<T extends Entity> {

    public static <T extends Entity> IndexDef<T> create(Class<T> klass, int fieldCount, Function<T, List<Value>> valuesFunc) {
        return new IndexDef<>(klass, fieldCount, false, valuesFunc);
    }

    public static <T extends Entity> IndexDef<T> createUnique(Class<T> klass, int fieldCount, Function<T, List<Value>> valuesFunc) {
        return new IndexDef<>(klass, fieldCount, true, valuesFunc);
    }

    @Getter
    private final Class<T> type;
    @Getter
    private final int fieldCount;
    private final Function<T, List<Value>> valuesFunc;
    @Getter
    private final boolean unique;
    @Setter
    @Getter
    private Index index;

    private IndexDef(Class<T> type, int fieldCount, boolean unique, Function<T, List<Value>> valuesFunc) {
        this.type = type;
        this.fieldCount = fieldCount;
        this.unique = unique;
        this.valuesFunc = valuesFunc;
    }

    public EntityIndexQueryBuilder<T> newQueryBuilder() {
        return EntityIndexQueryBuilder.newBuilder(this);
    }


    public @NotNull List<Value> getValues(org.metavm.entity.Entity entity) {
        return Objects.requireNonNull(valuesFunc.apply(type.cast(entity)),
                () -> "Failed to get index key values for " + type.getName() + "." + index
        );
    }

}
