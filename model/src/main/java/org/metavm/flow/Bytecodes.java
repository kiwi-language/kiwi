package org.metavm.flow;

import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Bytecodes {

    public static final int ADD_OBJECT = 3;
    public static final int SET_FIELD = 4;
    public static final int DELETE_OBJECT = 5;
    public static final int RETURN = 9;
    public static final int RAISE = 10;
    public static final int METHOD_CALL = 12;
    public static final int GET_UNIQUE = 13;
    public static final int NEW = 15;
    public static final int SET_STATIC = 17;
    public static final int NEW_ARRAY = 21;
    public static final int TRY_ENTER = 23;
    public static final int TRY_EXIT = 24;
    public static final int FUNC = 25;
    public static final int LAMBDA = 26;
    public static final int ADD_ELEMENT = 27;
    public static final int DELETE_ELEMENT = 28;
    public static final int GET_ELEMENT = 29;
    public static final int FUNCTION_CALL = 30;
    public static final int CAST = 31;
    public static final int CLEAR_ARRAY = 32;
    public static final int COPY = 33;
    public static final int INDEX_SCAN = 36;
    public static final int INDEX_COUNT = 37;
    public static final int INDEX_SELECT = 38;
    public static final int INDEX_SELECT_FIRST = 39;
    public static final int GOTO = 40;
    public static final int TARGET = 41;
    public static final int NON_NULL = 42;
    public static final int SET_ELEMENT = 43;
    public static final int IF_NE = 44;
    public static final int NOOP = 46;
    public static final int LONG_ADD = 48;
    public static final int LONG_SUB = 49;
    public static final int LONG_MUL = 50;
    public static final int LONG_DIV = 51;
    public static final int LONG_SHIFT_LEFT = 52;
    public static final int LONG_SHIFT_RIGHT = 53;
    public static final int LONG_UNSIGNED_SHIFT_RIGHT = 54;
    public static final int LONG_BIT_OR = 55;
    public static final int LONG_BIT_AND = 56;
    public static final int LONG_BIT_XOR = 57;
    public static final int LONG_NEG = 62;
    public static final int LONG_REM = 63;
    public static final int EQ = 64;
    public static final int NE = 65;
    public static final int GE = 66;
    public static final int GT = 67;
    public static final int LT = 68;
    public static final int LE = 69;
    public static final int GET_PROPERTY = 70;
    public static final int GET_STATIC = 71;
    public static final int INSTANCE_OF = 72;
    public static final int ARRAY_LENGTH = 73;
    public static final int IF_EQ = 74;
    public static final int STORE = 75;
    public static final int LOAD = 76;
    public static final int LOAD_CONTEXT_SLOT = 77;
    public static final int STORE_CONTEXT_SLOT = 78;
    public static final int LOAD_CONSTANT = 79;
    public static final int NEW_ARRAY_WITH_DIMS = 80;
    public static final int VOID_RETURN = 81;
    public static final int LOAD_TYPE = 82;
    public static final int DUP = 83;
    public static final int POP = 84;
    public static final int DUP_X1 = 85;
    public static final int DUP_X2 = 86;
    public static final int LOAD_PARENT = 87;
    public static final int NEW_CHILD = 88;
    public static final int LONG_TO_DOUBLE = 89;
    public static final int DOUBLE_TO_LONG = 90;
    public static final int DOUBLE_ADD = 91;
    public static final int DOUBLE_SUB = 92;
    public static final int DOUBLE_MUL = 93;
    public static final int DOUBLE_DIV = 94;
    public static final int DOUBLE_REM = 95;
    public static final int DOUBLE_NEG = 96;
    public static final int INT_TO_DOUBLE = 97;
    public static final int DOUBLE_TO_INT = 98;
    public static final int INT_TO_LONG = 99;
    public static final int LONG_TO_INT = 100;
    public static final int INT_ADD = 101;
    public static final int INT_SUB = 102;
    public static final int INT_MUL = 103;
    public static final int INT_DIV = 104;
    public static final int INT_REM = 105;
    public static final int INT_NEG = 106;
    public static final int INT_SHIFT_LEFT = 107;
    public static final int INT_SHIFT_RIGHT = 108;
    public static final int INT_UNSIGNED_SHIFT_RIGHT = 109;
    public static final int INT_BIT_AND = 110;
    public static final int INT_BIT_OR = 111;
    public static final int INT_BIT_XOR = 112;
    public static final int LONG_COMPARE = 113;
    public static final int INT_COMPARE = 114;
    public static final int DOUBLE_COMPARE = 115;
    public static final int REF_COMPARE_EQ = 116;
    public static final int REF_COMPARE_NE = 117;
    public static final int FLOAT_ADD = 118;
    public static final int FLOAT_SUB = 119;
    public static final int FLOAT_MUL = 120;
    public static final int FLOAT_DIV = 121;
    public static final int FLOAT_REM = 122;
    public static final int FLOAT_NEG = 123;
    public static final int FLOAT_COMPARE = 124;
    public static final int FLOAT_TO_INT = 125;
    public static final int FLOAT_TO_LONG = 126;
    public static final int FLOAT_TO_DOUBLE = 127;
    public static final int INT_TO_FLOAT = 128;
    public static final int LONG_TO_FLOAT = 129;
    public static final int DOUBLE_TO_FLOAT = 130;
    public static final int INT_TO_SHORT = 131;
    public static final int INT_TO_BYTE = 132;
    public static final int INT_TO_CHAR = 133;
    public static final int TABLE_SWITCH = 134;
    public static final int LOOKUP_SWITCH = 135;


    private static final String[] names = new String[256];

    public static String getBytecodeName(int code) {
        return names[code];
    }

    static {
        for (Field field : Bytecodes.class.getFields()) {
            if(Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                var code = ReflectionUtils.getIntField(field, null);
                names[code] = field.getName();
            }
        }
    }

}
