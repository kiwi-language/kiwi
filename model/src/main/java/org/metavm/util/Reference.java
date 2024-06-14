package org.metavm.util;

public record Reference(Object source, String fieldName, Object target) {

    @Override
    public String toString() {
        return source + "-" + fieldName + "->" + target;
    }
}
