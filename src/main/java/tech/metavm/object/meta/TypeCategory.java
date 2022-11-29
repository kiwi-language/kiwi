package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.infra.RegionInfo;
import tech.metavm.infra.RegionManager;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;

@EntityType("类型分类")
public enum TypeCategory {
    @EnumConstant("类")
    CLASS(0, SQLColumnType.INT64),
    @EnumConstant("枚举")
    ENUM(1, SQLColumnType.INT64/*, EnumConstantRT.class*/),
    @EnumConstant("接口")
    INTERFACE(2),
    @EnumConstant("值")
    VALUE(3, SQLColumnType.TEXT),
    @EnumConstant("基础")
    PRIMITIVE(6),
    @EnumConstant("流程输入")
    FLOW_INPUT(3),
    @EnumConstant("流程输出")
    FLOW_OUTPUT(9),
    @EnumConstant("页面")
    PAGE(4),
    @EnumConstant("数组")
    ARRAY(5),
    @EnumConstant("可空")
    NULLABLE(7),
    @EnumConstant("并集")
    UNION(9),
    @EnumConstant("空")
    NULL(10),
    @EnumConstant("字符串")
    STRING(11, SQLColumnType.VARCHAR64),
    @EnumConstant("浮点数")
    DOUBLE(12, SQLColumnType.FLOAT),
    @EnumConstant("长整数")
    LONG(13, SQLColumnType.INT64),
    @EnumConstant("布尔")
    BOOL(16, SQLColumnType.BOOL),
    @EnumConstant("时间")
    TIME(18, SQLColumnType.INT64),
    @EnumConstant("日期")
    DATE(19, SQLColumnType.INT64),
    @EnumConstant("整数")
    INT(20, SQLColumnType.INT32),
    @EnumConstant("密码")
    PASSWORD(22, SQLColumnType.TEXT),
    @EnumConstant("对象")
    OBJECT(21),
    @EnumConstant("参数化类型")
    PARAMETERIZED(22),
    @EnumConstant("类型变量")
    VARIABLE(23),
    ;

    private final int code;
    private final @Nullable SQLColumnType columnType;
//    private final @Nullable Class<? extends Entity> entityType;


    TypeCategory(int code) {
        this(code, null);
    }

//    TypeCategory(int code, SQLColumnType columnType) {
//        this(code, columnType, null);
//    }

    TypeCategory(int code, @Nullable SQLColumnType columnType/*, @Nullable Class<? extends Entity> entityType*/) {
        this.code = code;
        this.columnType = columnType;
//        this.entityType = entityType;
    }

    public static TypeCategory getByCodeRequired(int code) {
        return Arrays.stream(values()).filter(t -> t.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("模型类型不存在： " + code));
    }

//    public static @Nullable Class<? extends Entity> getEntityType(long id) {
//        TypeCategory match = NncUtils.find(values(), value -> value.idRangeContains(id));
//        return NncUtils.get(match, m -> m.entityType);
//    }

//    public @Nullable Class<? extends Entity> getEntityType() {
//        return entityType;
//    }

    public int code() {
        return code;
    }

    public boolean isReference() {
        return isClass() || isArray() || isEnum() || isValue();
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

    @SuppressWarnings("unused")
    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isPrimitive() {
        return this == PRIMITIVE || isString() || isDate() || isTime() || isNumber() || isInt32() || isInt64() || isBool();
    }

    public boolean idRangeContains(long id) {
        return NncUtils.orElse(getIdRegion(), r -> r.contains(id), () -> false);
    }

    public @Nullable RegionInfo getIdRegion() {
        return RegionManager.getRegionStatic(this);
    }

    public boolean isNullable() {
        return this == NULLABLE;
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

    public boolean isUnion() {
        return this == UNION;
    }

    public boolean isParameterized() {
        return this == PARAMETERIZED;
    }

    public boolean isTime() {
        return this == TIME;
    }

    public boolean isInt64() {
        return this == LONG;
    }

    public boolean isInt32() {
        return this == INT;
    }

    public boolean isNumber() {
        return this == DOUBLE;
    }

    public boolean isBool() {
        return this == BOOL;
    }

    @SuppressWarnings("unused")
    public boolean isComposite() {
        return !isPrimitive();
    }

    @SuppressWarnings("unused")
    public boolean isNotNull() {
        return !isNullable();
    }

    public boolean isEntity() {
        return this == CLASS;
    }

    @SuppressWarnings("unused")
    public @Nullable SQLColumnType getColumnType() {
        return columnType;
    }

    public boolean isVariable() {
        return this == VARIABLE;
    }
}
