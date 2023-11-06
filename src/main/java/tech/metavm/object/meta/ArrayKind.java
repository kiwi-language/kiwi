package tech.metavm.object.meta;

import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.util.*;

public enum ArrayKind {

    READ_WRITE(1, TypeCategory.READ_WRITE_ARRAY, ReadWriteArray.class),
    READ_ONLY(2, TypeCategory.READ_ONLY_ARRAY, ReadonlyArray.class),
    CHILD(3, TypeCategory.CHILD_ARRAY, ChildArray.class),

    ;

    private final int code;
    private final TypeCategory category;
    private final Class<?> entityClass;

    ArrayKind(int code, TypeCategory category, Class<?> entityClass) {
        this.code = code;
        this.category = category;
        this.entityClass = entityClass;
    }

    public static ArrayKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }

    public TypeCategory category() {
        return category;
    }

    public boolean isAssignableFrom(ArrayKind that) {
        return this == that || this == READ_ONLY;
    }

    public Class<? extends ReadonlyArray> getEntityClass() {
        //noinspection unchecked
        return (Class<? extends ReadonlyArray<?>>) entityClass;
    }

    public static ArrayKind getByEntityClass(Class<?> klass) {
        return NncUtils.findRequired(values(), v -> v.entityClass == klass);
    }

}
