package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.infra.RegionInfo;
import tech.metavm.infra.RegionManager;
import tech.metavm.object.instance.SQLType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;

@EntityType("类型分类")
public enum TypeCategory {
    @EnumConstant("类")
    CLASS(0, SQLType.REFERENCE),
    @EnumConstant("枚举")
    ENUM(1, SQLType.REFERENCE),
    @EnumConstant("接口")
    INTERFACE(2, SQLType.ANY),
    @EnumConstant("值")
    VALUE(3, SQLType.VALUE),
    @EnumConstant("数组")
    ARRAY(5, SQLType.MULTI_REFERENCE),
    @EnumConstant("并集")
    UNION(9, SQLType.UNION),
    @EnumConstant("空")
    NULL(10, SQLType.NULL),
    @EnumConstant("字符串")
    STRING(11, SQLType.VARCHAR64),
    @EnumConstant("浮点数")
    DOUBLE(12, SQLType.FLOAT),
    @EnumConstant("长整数")
    LONG(13, SQLType.INT64),
    @EnumConstant("布尔")
    BOOLEAN(16, SQLType.BOOL),
    @EnumConstant("时间")
    TIME(18, SQLType.INT64),
    @EnumConstant("日期")
    DATE(19, SQLType.INT64),
    @EnumConstant("整数")
    INT(20, SQLType.INT32),
    @EnumConstant("密码")
    PASSWORD(22, SQLType.TEXT),
    @EnumConstant("任意类型")
    ANY(21, SQLType.ANY),
    ;

    private final int code;
    private final SQLType sqlType;


    TypeCategory(int code, SQLType sqlType) {
        this.code = code;
        this.sqlType = sqlType;
    }

    public static TypeCategory getByCodeRequired(int code) {
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
        return this == ANY;
    }

    public boolean isPrimitive() {
        return isString() || isDate() || isTime() || isDouble()
                || isInt() || isLong() || isBool() || isPassword() || isNull();
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

    public boolean isInt() {
        return this == INT;
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

}
