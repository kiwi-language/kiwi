package org.manul.util;

public record KeyValue<K,V>(
        K key,
        V value
) {
}
