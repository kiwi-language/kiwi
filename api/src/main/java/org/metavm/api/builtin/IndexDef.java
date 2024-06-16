package org.metavm.api.builtin;

import java.util.List;

@SuppressWarnings("InstantiationOfUtilityClass")
public class IndexDef<T> {

    public static <T> IndexDef<T> create(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, false, List.of(fieldNames));
    }

    public static <T> IndexDef<T> createUnique(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, true, List.of(fieldNames));
    }

    public IndexDef(Class<T> klass, boolean unique, List<String> fieldNames) {
    }


}
