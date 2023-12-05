package tech.metavm.util;

public record IdAndValue<V> (
        long id,
        V value
) {
}
