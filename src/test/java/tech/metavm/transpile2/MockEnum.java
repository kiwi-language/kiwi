package tech.metavm.transpile2;

public enum MockEnum {

    M1(1),
    M2(2),
    M3(3),

    ;

    private final int code;

    MockEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
