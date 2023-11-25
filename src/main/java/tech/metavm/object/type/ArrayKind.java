package tech.metavm.object.type;

import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.util.*;

public enum ArrayKind {

    READ_WRITE(1, TypeCategory.READ_WRITE_ARRAY, ReadWriteArray.class) {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType) {
            return that == READ_WRITE && assignedElementType.contains(assignmentElementType);
        }
    },
    READ_ONLY(2, TypeCategory.READ_ONLY_ARRAY, ReadonlyArray.class) {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType) {
            return assignedElementType.isAssignableFrom(assignmentElementType);
        }
    },
    CHILD(3, TypeCategory.CHILD_ARRAY, ChildArray.class) {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType) {
            return that == CHILD && assignedElementType.contains(assignmentElementType);
        }
    },

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

    public abstract boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType);

    @SuppressWarnings("rawtypes")
    public Class<? extends ReadonlyArray> getEntityClass() {
        //noinspection unchecked
        return (Class<? extends ReadonlyArray<?>>) entityClass;
    }

    public static ArrayKind getByEntityClass(Class<?> klass) {
        return NncUtils.findRequired(values(), v -> v.entityClass == klass);
    }

}
