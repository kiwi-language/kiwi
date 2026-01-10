package org.manul.util;

public record IdAndValue<V> (
        long id,
        V value
) {
}
