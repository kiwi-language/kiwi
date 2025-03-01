package org.metavm.compiler.type;

public class TypeTags {
    public static final int TAG_NEVER = 1;
    public static final int TAG_VARIABLE = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTE = 7;
    public static final int TAG_SHORT = 8;
    public static final int TAG_CHAR = 9;
    public static final int TAG_BOOLEAN = 10;
    public static final int TAG_STRING = 11;
    public static final int TAG_VOID = 12;
    public static final int TAG_NULL = 13;
    public static final int TAG_TIME = 14;
    public static final int TAG_PASSWORD = 15;
    public static final int TAG_CLASS = 1 << 15;
    public static final int TAG_ARRAY = 1 << 16;
    public static final int TAG_FUNCTION = 1 << 17;
    public static final int TAG_UNION = 1 << 18;
    public static final int TAG_INTERSECTION = 1 << 19;
    public static final int TAG_UNCERTAIN = 1 << 20;

    public static final int TAG_ANY = 1 << 11;

}
