package org.metavm.entity;

import lombok.Getter;

import java.util.Objects;
import java.util.function.Supplier;

@Getter
public class AttributeKey<T> {

    private final Class<T> type;
    private final boolean nullable;
    private final Supplier<T> defaultValueSupplier;

    public AttributeKey(Class<T> type, boolean nullable, Supplier<T> defaultValueSupplier) {
        this.type = type;
        this.nullable = nullable;
        if(!nullable) {
            Objects.requireNonNull(defaultValueSupplier);
            Objects.requireNonNull(defaultValueSupplier.get());
        }
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public boolean isNotNull() {
        return !isNullable();
    }


}
