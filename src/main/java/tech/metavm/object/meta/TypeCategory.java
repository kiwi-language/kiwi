package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.entity.natives.ArrayNative;
import tech.metavm.infra.RegionInfo;
import tech.metavm.infra.RegionManager;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.rest.dto.ArrayTypeParamDTO;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.TypeVariableParamDTO;
import tech.metavm.object.meta.rest.dto.UnionTypeParamDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

@EntityType("类型分类")
public enum TypeCategory {
    @EnumConstant("类")
    CLASS(0, SQLType.REFERENCE, ClassParamDTO.class),
    @EnumConstant("枚举")
    ENUM(1, SQLType.REFERENCE, ClassParamDTO.class),
    @EnumConstant("接口")
    INTERFACE(2, SQLType.OBJECT, ClassParamDTO.class),
    @EnumConstant("值")
    VALUE(3, SQLType.VALUE, ClassParamDTO.class),
    @EnumConstant("数组")
    ARRAY(5, SQLType.MULTI_REFERENCE, ArrayTypeParamDTO.class, ArrayNative.class),
    @EnumConstant("并集")
    UNION(9, SQLType.UNION, UnionTypeParamDTO.class),
    @EnumConstant("空")
    NULL(10, SQLType.NULL),
    @EnumConstant("字符串")
    STRING(11, SQLType.VARCHAR64),
    @EnumConstant("浮点数")
    DOUBLE(12, SQLType.FLOAT),
    @EnumConstant("整数")
    LONG(13, SQLType.INT64),
    @EnumConstant("布尔")
    BOOLEAN(16, SQLType.BOOL),
    @EnumConstant("时间")
    TIME(18, SQLType.INT64),
    @EnumConstant("日期")
    DATE(19, SQLType.INT64),
    @EnumConstant("密码")
    PASSWORD(22, SQLType.TEXT),
    @EnumConstant("任意类型")
    OBJECT(21, SQLType.OBJECT),
    @EnumConstant("实例")
    INSTANCE(21, SQLType.REFERENCE),
    @EnumConstant("Void")
    VOID(23, SQLType.VALUE),
    @EnumConstant("类型变量")
    VARIABLE(24, SQLType.OBJECT, TypeVariableParamDTO.class),
    @EnumConstant("参数化类型")
    PARAMETERIZED(25, SQLType.OBJECT),
    @EnumConstant("类型交集")
    INTERSECTION(26, SQLType.OBJECT),
    FUNCTION(27, SQLType.OBJECT),

    ;

    private final int code;
    private final SQLType sqlType;
    private final Class<?> paramClass;
    private final @Nullable Class<?> nativeClass;

    TypeCategory(int code, SQLType sqlType) {
        this(code, sqlType, null);
    }

    TypeCategory(int code, SQLType sqlType, Class<?> paramClass) {
        this(code, sqlType, paramClass, null);
    }

    TypeCategory(int code, SQLType sqlType, Class<?> paramClass, @Nullable Class<?> nativeClass) {
        this.code = code;
        this.sqlType = sqlType;
        this.paramClass = paramClass;
        this.nativeClass = nativeClass;
    }

    public static TypeCategory getByCode(int code) {
        return Arrays.stream(values()).filter(t -> t.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("模型类型不存在： " + code));
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
        return this == ARRAY;
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

    public static TypeCategory getByParamClassRequired(Class<?> paramKlass) {
        return Arrays.stream(values())
                .filter(type -> Objects.equals(type.getParamClass(), paramKlass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("TypeCategory not found for param class: " + paramKlass.getName()));
    }

}
