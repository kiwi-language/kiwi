package tech.metavm.object.meta;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;
import tech.metavm.util.Password;

import java.util.Date;

public enum PrimitiveKind {
//    INT("整数", Integer.class, IntInstance.class, TypeCategory.INT),
    LONG("长整数", Long.class, LongInstance.class, TypeCategory.LONG),
    DOUBLE("浮点数", Double.class, DoubleInstance.class, TypeCategory.DOUBLE),
    BOOLEAN("布尔", Boolean.class, BooleanInstance.class, TypeCategory.BOOLEAN),
    STRING("字符串", String.class, StringInstance.class, TypeCategory.STRING),
    TIME("时间", Date.class, TimeInstance.class, TypeCategory.TIME),
    PASSWORD("密码", Password.class, PasswordInstance.class, TypeCategory.PASSWORD),
    NULL("空", Null.class, NullInstance.class, TypeCategory.NULL),
    VOID("Void", Void.class, null, TypeCategory.VOID)

    ;

    private final String name;
    private final Class<?> javaClass;
    private final Class<? extends Instance> instanceClass;
    private final TypeCategory typeCategory;

    PrimitiveKind(String name, Class<?> javaClass, Class<? extends Instance> instanceClass, TypeCategory typeCategory) {
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

}
