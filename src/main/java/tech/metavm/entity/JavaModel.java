package tech.metavm.entity;

import java.lang.reflect.Type;

public record JavaModel<T>(T value, Type type) {

    public <R> JavaModel<R> cast(Class<R> klass) {
        return new JavaModel<>(
                klass.cast(value),
                type
        );
    }

}
