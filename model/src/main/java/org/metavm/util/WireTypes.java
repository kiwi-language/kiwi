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

    public static final int REFERENCE = 10;
    public static final int INSTANCE = 11;
    public static final int VALUE_INSTANCE = 12;
    public static final int FLAGGED_REFERENCE = 13;
    public static final int RELOCATING_INSTANCE = 14;
    public static final int REDIRECTING_INSTANCE = 15;
    public static final int REDIRECTING_REFERENCE = 16;
    public static final int REMOVING_INSTANCE = 17;


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
