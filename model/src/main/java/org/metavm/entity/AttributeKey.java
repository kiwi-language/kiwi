package org.metavm.entity;

import org.metavm.util.TypeReference;

import java.util.Objects;
import java.util.function.Supplier;

public class AttributeKey<T> {

    private final Class<T> type;
    private final boolean nullable;
    private final Supplier<T> defaultValueSupplier;

    public AttributeKey(TypeReference<T> typeReference, boolean nullable, Supplier<T> defaultValueSupplier) {
        this(typeReference.getType(), nullable, defaultValueSupplier);
    }

    public AttributeKey(Class<T> type, boolean nullable, Supplier<T> defaultValueSupplier) {
        this.type = type;
        this.nullable = nullable;
        if(!nullable) {
            Objects.requireNonNull(defaultValueSupplier);
            Objects.requireNonNull(defaultValueSupplier.get());
        }
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public Class<T> getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    public Supplier<T> getDefaultValueSupplier() {
        return defaultValueSupplier;
    }


}
