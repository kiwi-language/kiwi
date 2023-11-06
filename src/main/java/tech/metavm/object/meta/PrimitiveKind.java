package tech.metavm.object.meta;

import tech.metavm.object.instance.core.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;
import tech.metavm.util.Password;

import java.util.Date;

public enum PrimitiveKind {
    LONG(1, "整数", Long.class, LongInstance.class, TypeCategory.LONG),
    DOUBLE(2, "浮点数", Double.class, DoubleInstance.class, TypeCategory.DOUBLE),
    STRING(3, "字符串", String.class, StringInstance.class, TypeCategory.STRING),
    BOOLEAN(4, "布尔", Boolean.class, BooleanInstance.class, TypeCategory.BOOLEAN),
    TIME(5, "时间", Date.class, TimeInstance.class, TypeCategory.TIME),
    PASSWORD(6, "密码", Password.class, PasswordInstance.class, TypeCategory.PASSWORD),
    NULL(7, "空", Null.class, NullInstance.class, TypeCategory.NULL),
    VOID(8, "无", Void.class, null, TypeCategory.VOID)

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

    public int getCode() {
        return code;
    }

    public static PrimitiveKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
