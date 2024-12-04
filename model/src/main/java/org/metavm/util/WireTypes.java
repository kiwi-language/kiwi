package org.metavm.util;

public class WireTypes {
    public static final int NULL = 0;
    public static final int LONG = 1;
    public static final int DOUBLE = 2;
    public static final int STRING = 3;
    public static final int BOOLEAN = 4;
    public static final int TIME = 5;
    public static final int PASSWORD = 6;
    public static final int CHAR = 7;
    public static final int INT = 8;
    public static final int FLOAT = 9;

    public static final int REFERENCE = 10;
    public static final int INSTANCE = 11;
    public static final int VALUE_INSTANCE = 12;
    public static final int FLAGGED_REFERENCE = 13;
    public static final int RELOCATING_INSTANCE = 14;
    public static final int REDIRECTING_INSTANCE = 15;
    public static final int REDIRECTING_REFERENCE = 16;
    public static final int REMOVING_INSTANCE = 17;

    // Types
    public static final int CLASS_TYPE = 21;
    public static final int PARAMETERIZED_TYPE = 22;
    public static final int UNION_TYPE = 23;
    public static final int VARIABLE_TYPE = 24;
    public static final int INTERSECTION_TYPE = 25;
    public static final int ANY_TYPE = 26;
    public static final int NEVER_TYPE = 27;
    public static final int FUNCTION_TYPE = 28;
    public static final int UNCERTAIN_TYPE = 29;
    public static final int LONG_TYPE = 30;
    public static final int DOUBLE_TYPE = 31;
    public static final int TIME_TYPE = 32;
    public static final int NULL_TYPE = 33;
    public static final int VOID_TYPE = 34;
    public static final int PASSWORD_TYPE = 35;
    public static final int BOOLEAN_TYPE = 36;
    public static final int STRING_TYPE = 37;
    public static final int CHAR_TYPE = 38;
    public static final int READ_ONLY_ARRAY_TYPE = 39;
    public static final int READ_WRITE_ARRAY_TYPE = 40;
    public static final int CHILD_ARRAY_TYPE = 41;
    public static final int CAPTURED_TYPE = 42;
    public static final int TAGGED_CLASS_TYPE = 43;
    public static final int VALUE_ARRAY_TYPE = 44;
    public static final int INT_TYPE = 45;
    public static final int FLOAT_TYPE = 46;


    // Element references
    public static final int METHOD_REF = 70;
    public static final int FUNCTION_REF = 71;
    public static final int FIELD_REF = 72;
    public static final int INDEX_REF = 73;
    public static final int LAMBDA_REF = 74;

//    LONG(1),
//    DOUBLE(2),
//    STRING(3),
//    BOOLEAN(4),
//    TIME(5),
//    PASSWORD(6),
//    NULL(7),
//    REFERENCE(10),
//    RECORD(11),


    ;

//    private final int code;
//
//    WireType(int code) {
//        this.code = code;
//    }
//
//    public static WireType getByCode(int code) {
//        return NncUtils.findRequired(values(), v -> v.code() == code);
//    }
//
//    public int code() {
//        return code;
//    }

}
