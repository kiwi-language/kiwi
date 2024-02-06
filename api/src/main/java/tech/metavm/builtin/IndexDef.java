package tech.metavm.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndexDef<T> {

    public static <T> IndexDef<T> create(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, false, List.of(fieldNames));
    }

    public static <T> IndexDef<T> createUnique(Class<T> klass, String...fieldNames) {
        return new IndexDef<>(klass, true, List.of(fieldNames));
    }

    private final Class<T> klass;
    private final boolean unique;
    private final List<String> fieldNames;

    public IndexDef(Class<T> klass, boolean unique, List<String> fieldNames) {
        this.klass = klass;
        this.unique = unique;
        this.fieldNames = new ArrayList<>(fieldNames);
    }

    public Class<T> getKlass() {
        return klass;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<String> getFieldNames() {
        return Collections.unmodifiableList(fieldNames);
    }
}
