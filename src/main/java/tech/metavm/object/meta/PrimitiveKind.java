package tech.metavm.object.meta;

import tech.metavm.util.Null;
import tech.metavm.util.Password;

import java.util.Date;

public enum PrimitiveKind {
    INT("整数", Integer.class, TypeCategory.INT),
    LONG("长整数", Long.class, TypeCategory.LONG),
    DOUBLE("浮点数", Double.class, TypeCategory.DOUBLE),
    BOOLEAN("布尔", Boolean.class, TypeCategory.BOOLEAN),
    STRING("字符串", String.class, TypeCategory.STRING),
    TIME("时间", Date.class, TypeCategory.TIME),
    PASSWORD("密码", Password.class, TypeCategory.PASSWORD),
    NULL("空", Null.class, TypeCategory.NULL)

    ;

    private final String name;
    private final Class<?> valueClass;
    private final TypeCategory typeCategory;

    PrimitiveKind(String name, Class<?> valueClass, TypeCategory typeCategory) {
        this.name = name;
        this.valueClass = valueClass;
        this.typeCategory = typeCategory;
    }

    public String getName() {
        return name;
    }

    public boolean checkValue(Object value) {
        return value != null && value.getClass() == valueClass;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }
}
