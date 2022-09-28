//package tech.metavm.object.meta;
//
//import tech.metavm.object.instance.ColumnType;
//import tech.metavm.util.BusinessException;
//import tech.metavm.util.Column;
//
//import java.util.Arrays;
//
//public enum FieldType {
//
//    STRING(1, ColumnType.VARCHAR64),
//    NUMBER(2, ColumnType.FLOAT),
//    INT64(3, ColumnType.INT64),
//    BOOL(6, ColumnType.BOOL),
//    CHOICE(7, ColumnType.RELATION),
//    TIME(8, ColumnType.INT64),
//    DATE(9, ColumnType.INT64),
//    RELATION(10, ColumnType.RELATION),
//    CHILD_RELATION(11, ColumnType.RELATION),
//    INT32(12, ColumnType.INT32),
//
//    ;
//
//    private final int code;
//
//    private final ColumnType columnType;
//
//    FieldType(int code, ColumnType columnType) {
//        this.code = code;
//        this.columnType = columnType;
//    }
//
//    public static FieldType getByCodeRequired(int code) {
//        return Arrays.stream(values())
//                .filter(t -> t.code == code)
//                .findAny()
//                .orElseThrow(() -> BusinessException.invalidParams("属性类型不存在: " + code));
//    }
//
//    public static FieldType getByCode(int code) {
//        return Arrays.stream(values())
//                .filter(t -> t.code == code)
//                .findAny()
//                .orElse(null);
//    }
//
//    public static boolean isChoice(int code) {
//        FieldType category = getByCode(code);
//        return category != null && category.isChoice();
//    }
//
//    public static boolean isSingleChoice(int code) {
//        FieldType category = getByCode(code);
//        return category != null && category.isSingleChoice();
//    }
////
////    public static boolean isMultiChoice(int code) {
////        FieldType category = getByCode(code);
////        return category != null && category.isMultiChoice();
////    }
//
//    public boolean isGeneralRelation() {
//        return isRelation() || isChoice() || isChildRelation();
//    }
//
//    public boolean isGeneralSingleRelation() {
//        return isSingleRelation() || isSingleChoice();
//    }
//
//    public boolean isRelation() {
//        return this == RELATION;
//    }
//
//    public boolean isChildRelation() {
//        return this == CHILD_RELATION;
//    }
//
//    public boolean isSingleRelation() {
//        return this == RELATION;
//    }
//
//    public boolean isChoice() {
//        return this == CHOICE;
//    }
//
//    public boolean isSingleChoice() {
//        return this == CHOICE;
//    }
//
//    public int code() {
//        return code;
//    }
//
//    public boolean isInt64() {
//        return this == INT64;
//    }
//
//    public boolean isNumber() {
//        return this == NUMBER;
//    }
//
//    public boolean isBool() {
//        return this == BOOL;
//    }
//
//    public ColumnType getColumnType() {
//        return columnType;
//    }
//
//    public boolean checkColumn(Column column) {
//        return column != null && columnType == column.category();
//    }
//
//    public boolean isString() {
//        return this == STRING;
//    }
//
//    public boolean isTime() {
//        return this == DATE;
//    }
//
//    public boolean isInt32() {
//        return this == INT32;
//    }
//}
