package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.entity.natives.ArrayNative;
import tech.metavm.management.RegionInfo;
import tech.metavm.management.RegionManager;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@EntityType("类型分类")
public enum TypeCategory {
    @EnumConstant("类")
    CLASS(0, SQLType.REFERENCE, ClassTypeParam.class, 2),
    @EnumConstant("枚举")
    ENUM(1, SQLType.REFERENCE, ClassTypeParam.class, 2),
    @EnumConstant("接口")
    INTERFACE(2, SQLType.OBJECT, ClassTypeParam.class, 3),
    @EnumConstant("值")
    VALUE(3, SQLType.VALUE, ClassTypeParam.class,2 ),
    @EnumConstant("读写数组")
    READ_WRITE_ARRAY(6, SQLType.MULTI_REFERENCE, ArrayTypeParam.class, ArrayNative.class, 4),
    @EnumConstant("只读数组")
    READ_ONLY_ARRAY(7, SQLType.MULTI_REFERENCE, ArrayTypeParam.class, ArrayNative.class, 4),
    @EnumConstant("子对象数组")
    CHILD_ARRAY(8, SQLType.MULTI_REFERENCE, ArrayTypeParam.class, ArrayNative.class, 4),
    @EnumConstant("并集")
    UNION(9, SQLType.UNION, UnionTypeParam.class, 5),
    @EnumConstant("空")
    NULL(10, SQLType.NULL, PrimitiveTypeParam.class, 5),
    @EnumConstant("字符串")
    STRING(11, SQLType.VARCHAR64, PrimitiveTypeParam.class, 5),
    @EnumConstant("浮点数")
    DOUBLE(12, SQLType.FLOAT, PrimitiveTypeParam.class, 5),
    @EnumConstant("整数")
    LONG(13, SQLType.INT64, PrimitiveTypeParam.class, 5),
    @EnumConstant("布尔")
    BOOLEAN(16, SQLType.BOOL, PrimitiveTypeParam.class, 5),
    @EnumConstant("时间")
    TIME(18, SQLType.INT64, PrimitiveTypeParam.class, 5),
    @EnumConstant("日期")
    DATE(19, SQLType.INT64, PrimitiveTypeParam.class, 5),
    @EnumConstant("任意类型")
    OBJECT(21, SQLType.OBJECT, 5),
    @EnumConstant("密码")
    PASSWORD(22, SQLType.TEXT, PrimitiveTypeParam.class, 5),
    @EnumConstant("Void")
    VOID(23, SQLType.VALUE, PrimitiveTypeParam.class, 5),
    @EnumConstant("类型变量")
    VARIABLE(24, SQLType.OBJECT, TypeVariableParam.class, 1),
    @EnumConstant("类型交集")
    INTERSECTION(26, SQLType.OBJECT, 4),
    @EnumConstant("函数")
    FUNCTION(27, SQLType.OBJECT, FunctionTypeParam.class, 5),
    @EnumConstant("不确定")
    UNCERTAIN(28, SQLType.OBJECT, UncertainTypeParam.class, 5),
    @EnumConstant("不可能")
    NOTHING(29, SQLType.OBJECT, 5),

    ;

    private final int code;
    private final int closurePrecedence;
    private final SQLType sqlType;
    private final Class<?> paramClass;
    private final @Nullable Class<?> nativeClass;

    private static volatile Set<TypeCategory> ALL;

    TypeCategory(int code, SQLType sqlType, int closurePrecedence) {
        this(code, sqlType, null, closurePrecedence);
    }

    TypeCategory(int code, SQLType sqlType, Class<?> paramClass, int closurePrecedence) {
        this(code, sqlType, paramClass, null, closurePrecedence);
    }

    TypeCategory(int code, SQLType sqlType, Class<?> paramClass, @Nullable Class<?> nativeClass, int closurePrecedence) {
        this.code = code;
        this.sqlType = sqlType;
        this.paramClass = paramClass;
        this.nativeClass = nativeClass;
        this.closurePrecedence = closurePrecedence;
    }

    public static TypeCategory getByCode(int code) {
        return Arrays.stream(values()).filter(t -> t.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("模型类型不存在： " + code));
    }

    public static List<TypeCategory> arrayCategories() {
        return List.of(READ_WRITE_ARRAY, READ_ONLY_ARRAY, CHILD_ARRAY);
    }

    public static Set<TypeCategory> pojoCategories() {
        return Set.of(TypeCategory.CLASS, TypeCategory.VALUE, TypeCategory.INTERFACE);
    }

    public int code() {
        return code;
    }

    public boolean isClass() {
        return this == CLASS;
    }

    public boolean isEnum() {
        return this == ENUM;
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isValue() {
        return this == VALUE;
    }

    public boolean isArray() {
        return this == READ_ONLY_ARRAY || this == READ_WRITE_ARRAY || this == CHILD_ARRAY;
    }

    public boolean isObject() {
        return this == OBJECT;
    }

    public boolean isPrimitive() {
        return isString() || isDate() || isTime() || isDouble()
                || isLong() || isBool() || isPassword() || isNull();
    }

    public boolean idRangeContains(long id) {
        return NncUtils.mapOrElse(getIdRegion(), r -> r.contains(id), () -> false);
    }

    public @Nullable RegionInfo getIdRegion() {
        return RegionManager.getRegionStatic(this);
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isDate() {
        return this == DATE;
    }

    public boolean isDouble() {
        return this == DOUBLE;
    }

    public boolean isLong() {
        return this == LONG;
    }

    public boolean isPassword() {
        return this == PASSWORD;
    }

    @SuppressWarnings("unused")
    public boolean isUnion() {
        return this == UNION;
    }

    public boolean isTime() {
        return this == TIME;
    }

    public boolean isBool() {
        return this == BOOLEAN;
    }

    public boolean isEntity() {
        return this == CLASS;
    }

    public SQLType getSQLType() {
        return sqlType;
    }

    public Class<?> getParamClass() {
        return paramClass;
    }

    public boolean isInterface() {
        return this == INTERFACE;
    }

    public @Nullable Class<?> getNativeClass() {
        return nativeClass;
    }

    public boolean isPojo() {
        return this == INTERFACE || this == CLASS || this == ENUM;
    }

    public boolean isFunction() {
        return this == FUNCTION;
    }

    public boolean isUncertain() {
        return this == UNCERTAIN;
    }

    public boolean isIntersection() {
        return this == INTERSECTION;
    }

    public static Set<TypeCategory> all() {
        if(ALL == null) {
            ALL = Set.of(values());
        }
        return ALL;
    }

    public int closurePrecedence() {
        return closurePrecedence;
    }

    public static TypeCategory getByParamClassRequired(Class<?> paramKlass) {
        return Arrays.stream(values())
                .filter(type -> Objects.equals(type.getParamClass(), paramKlass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("TypeCategory not found for param class: " + paramKlass.getName()));
    }

}
