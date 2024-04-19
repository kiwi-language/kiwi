package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public enum ArrayKind {

    READ_WRITE(1, TypeCategory.READ_WRITE_ARRAY, ReadWriteArray.class, "[]") {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType, Map<TypeVariable, ? extends Type> typeMapping) {
             return (that == READ_WRITE || that == CHILD) && assignedElementType.contains(assignmentElementType, typeMapping);
        }

        @Override
        public String getInternalName(Type elementType, @Nullable Flow current) {
            return List.class.getName() + "<" + elementType.getInternalName(current) + ">";
        }

    },
    READ_ONLY(2, TypeCategory.READ_ONLY_ARRAY, ReadonlyArray.class, "[R]") {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType, Map<TypeVariable, ? extends Type> typeMapping) {
            return assignedElementType.isAssignableFrom(assignmentElementType, typeMapping);
        }

        @Override
        public String getInternalName(Type elementType, @Nullable Flow current) {
            return ReadonlyList.class.getName() + "<" + elementType.getInternalName(current) + ">";
        }
    },
    CHILD(3, TypeCategory.CHILD_ARRAY, ChildArray.class, "[C]") {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType, Map<TypeVariable, ? extends Type> typeMapping) {
            return that == CHILD && assignedElementType.contains(assignmentElementType, typeMapping);
        }

        @Override
        public String getInternalName(Type elementType, @Nullable Flow current) {
            return ChildList.class.getName() + "<" + elementType.getInternalName(current) + ">";
        }
    },

    ;

    private final int code;
    private final TypeCategory category;
    private final Class<?> entityClass;
    private final String suffix;

    ArrayKind(int code, TypeCategory category, Class<?> entityClass, String suffix) {
        this.code = code;
        this.category = category;
        this.entityClass = entityClass;
        this.suffix = suffix;
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

    public abstract boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType, Map<TypeVariable, ? extends Type> typeMapping);

    public abstract String getInternalName(Type elementType, @Nullable Flow current);

    @SuppressWarnings("rawtypes")
    public Class<? extends ReadonlyArray> getEntityClass() {
        //noinspection unchecked
        return (Class<? extends ReadonlyArray<?>>) entityClass;
    }

    public static ArrayKind getByEntityClass(Class<?> klass) {
        return NncUtils.findRequired(values(), v -> v.entityClass == klass);
    }

    public String getSuffix() {
        return suffix;
    }
}
