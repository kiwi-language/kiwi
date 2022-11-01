package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLColumnType;

import java.util.Arrays;

public enum TypeCategory {
    CLASS(0, SQLColumnType.INT64),
    ENUM(1, SQLColumnType.INT64),
    INTERFACE(2),
    PRIMITIVE(6),
    FLOW_INPUT(3),
    FLOW_OUTPUT(9),
    PAGE(4),
    ARRAY(5),
    NULLABLE(7),
    PARAMETERIZED(8),


    STRING(11, SQLColumnType.VARCHAR64),
    DOUBLE(12, SQLColumnType.FLOAT),
    LONG(13, SQLColumnType.INT64),
    BOOL(16, SQLColumnType.BOOL),
    TIME(18, SQLColumnType.INT64),
    DATE(19, SQLColumnType.INT64),
    INT(20, SQLColumnType.INT32),
    OBJECT(21),
    ;

    private final int code;
    private final SQLColumnType columnType;


    TypeCategory(int code) {
        this(code, null);
    }

    TypeCategory(int code, SQLColumnType columnType) {
        this.code = code;
        this.columnType = columnType;
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

    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isPrimitive() {
        return isString() || isDate() || isTime() || isNumber() || isInt32() || isInt64() || isBool();
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

    public boolean isComposite() {
        return !isPrimitive();
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    public boolean isEntity() {
        return this == CLASS;
    }
}
