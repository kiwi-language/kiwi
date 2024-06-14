package org.metavm.object.type;

import org.metavm.object.instance.core.*;
import org.metavm.util.NamingUtils;
import org.metavm.util.NncUtils;
import org.metavm.util.Null;
import org.metavm.util.Password;

import java.util.Date;

public enum PrimitiveKind {
    LONG(1, "long", Long.class, LongInstance.class, TypeCategory.LONG),
    DOUBLE(2, "double", Double.class, DoubleInstance.class, TypeCategory.DOUBLE),
    STRING(3, "string", String.class, StringInstance.class, TypeCategory.STRING),
    BOOLEAN(4, "boolean", Boolean.class, BooleanInstance.class, TypeCategory.BOOLEAN),
    TIME(5, "time", Date.class, TimeInstance.class, TypeCategory.TIME),
    PASSWORD(6, "password", Password.class, PasswordInstance.class, TypeCategory.PASSWORD),
    NULL(7, "null", Null.class, NullInstance.class, TypeCategory.NULL),
    VOID(8, "void", Void.class, null, TypeCategory.VOID)

    ;

    private final int code;
    private final String name;
    private final Class<?> javaClass;
    private final Class<? extends Instance> instanceClass;
    private final TypeCategory typeCategory;

    PrimitiveKind(int code, String name, Class<?> javaClass, Class<? extends Instance> instanceClass, TypeCategory typeCategory) {
        this.code = code;
        this.name = name;
        this.javaClass = javaClass;
        this.instanceClass = instanceClass;
        this.typeCategory = typeCategory;
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

    public Class<? extends Instance> getInstanceClass() {
        return instanceClass;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }

    public static PrimitiveKind getByJavaClass(Class<?> javaClass) {
        return NncUtils.findRequired(values(), v -> v.javaClass == javaClass);
    }

    public String getTypeCode() {
        return NamingUtils.firstCharToUpperCase(this.name().toLowerCase());
    }

    public int code() {
        return code;
    }

    public static PrimitiveKind fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
