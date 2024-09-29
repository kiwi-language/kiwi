package org.metavm.object.type;

import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Date;

public enum PrimitiveKind {
    LONG(1, "long", Long.class, LongValue.class, TypeCategory.LONG) {
        @Override
        public boolean isConvertibleFrom(PrimitiveKind kind) {
            return kind == DOUBLE;
        }

        @Override
        public Value convert(Value instance) {
            if (instance instanceof DoubleValue d)
                return Instances.longInstance(d.getValue().longValue());
            else
                throw new IllegalArgumentException();
        }

        @Override
        public Value getDefaultValue() {
            return Instances.longInstance(0L);
        }
    },
    DOUBLE(2, "double", Double.class, DoubleValue.class, TypeCategory.DOUBLE) {
        @Override
        public boolean isConvertibleFrom(PrimitiveKind kind) {
            return kind == LONG;
        }

        @Override
        public Value convert(Value instance) {
            if (instance instanceof LongValue l)
                return Instances.doubleInstance(l.getValue().doubleValue());
            else
                throw new IllegalArgumentException();
        }

        @Override
        public Value getDefaultValue() {
            return Instances.doubleInstance(0.0);
        }
    },
    STRING(3, "string", String.class, StringValue.class, TypeCategory.STRING),
    BOOLEAN(4, "boolean", Boolean.class, BooleanValue.class, TypeCategory.BOOLEAN) {
        @Override
        public Value getDefaultValue() {
            return Instances.booleanInstance(false);
        }
    },
    TIME(5, "time", Date.class, TimeValue.class, TypeCategory.TIME),
    PASSWORD(6, "password", Password.class, PasswordValue.class, TypeCategory.PASSWORD),
    NULL(7, "null", Null.class, NullValue.class, TypeCategory.NULL) {
        @Override
        public Value getDefaultValue() {
            return Instances.nullInstance();
        }
    },
    VOID(8, "void", Void.class, null, TypeCategory.VOID);

    private final int code;
    private final String name;
    private final Class<?> javaClass;
    private final Class<? extends Value> instanceClass;
    private final TypeCategory typeCategory;
    private final String typeCode;
    private PrimitiveType type;

    PrimitiveKind(int code, String name, Class<?> javaClass, Class<? extends Value> instanceClass, TypeCategory typeCategory) {
        this.code = code;
        this.name = name;
        this.javaClass = javaClass;
        this.instanceClass = instanceClass;
        this.typeCategory = typeCategory;
        this.typeCode = NamingUtils.firstCharToUpperCase(this.name().toLowerCase());
    }

    public String getName() {
        return name;
    }

    public boolean checkValue(Object value) {
        return value != null && value.getClass() == javaClass;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public Class<? extends Value> getInstanceClass() {
        return instanceClass;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }

    public static PrimitiveKind getByJavaClass(Class<?> javaClass) {
        return NncUtils.findRequired(values(), v -> v.javaClass == javaClass);
    }

    public String getTypeCode() {
        return typeCode;
    }

    public int code() {
        return code;
    }

    public static PrimitiveKind fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public PrimitiveType getType() {
        return type;
    }

    void setType(PrimitiveType type) {
        this.type = type;
    }

    public boolean isConvertibleFrom(PrimitiveKind kind) {
        return false;
    }

    public Value convert(Value instance) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Value getDefaultValue() {
        return null;
    }
}
