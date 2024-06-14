package org.metavm.util;

public record KeyValue<K,V>(
        K key,
        V value
) {
}
