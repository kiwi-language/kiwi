package tech.metavm.object.meta;

import tech.metavm.object.instance.ColumnType;

import java.util.Arrays;

public enum TypeCategory {
    TABLE(0),
    ENUM(1),
    INTERFACE(2),
    FLOW_INPUT(3),
    PAGE(4),
    ARRAY(5),
    NULLABLE(7),

    STRING(11, ColumnType.VARCHAR64),
    NUMBER(12, ColumnType.FLOAT),
    INT64(13, ColumnType.INT64),
    BOOL(16, ColumnType.BOOL),
    TIME(18, ColumnType.INT64),
    DATE(19, ColumnType.INT64),
    INT32(20, ColumnType.INT32),
    ;

    private final int code;
    private final ColumnType columnType;


    TypeCategory(int code) {
        this(code, null);
    }

    TypeCategory(int code, ColumnType columnType) {
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

    public boolean isTable() {
        return this == TABLE;
    }

    public boolean isEnum() {
        return this == ENUM;
    }

    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isPrimitive() {
        return isString() || isDate() || isTime() || isNumber() || isInt32() || isInt64() || isBool() ;
    }

    public boolean isNullable() {
        return this == NULLABLE;
    }

    public ColumnType getColumnType() {
        return columnType;
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
        return this == INT64;
    }

    public boolean isInt32() {
        return this == INT32;
    }

    public boolean isNumber() {
        return this == NUMBER;
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
}
