package enums;

public enum EnumFieldFoo {

    op1(1, "op1"),

    ;

    private final int code;
    private final String message;

    EnumFieldFoo(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public static String getOp1Message() {
        return op1.message;
    }

}
