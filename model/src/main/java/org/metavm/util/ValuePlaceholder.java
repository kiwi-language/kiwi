package org.metavm.util;

import org.jetbrains.annotations.Nullable;

public class ValuePlaceholder<T> {

    private T value;

    public T get() {
        if(value == null) {
            throw new IllegalStateException("Value is not set yet");
        }
        return value;
    }

    public void set(T value) {
        if(this.value != null) {
            throw new IllegalStateException("Value is already set");
        }
        this.value = value;
    }

    public boolean isSet() {
        return this.value != null;
    }

    @Nullable
    public T orElseNull() {
        return isSet() ? value : null;
    }

}
