package tech.metavm.spoon;

public enum EnumFoo {

    FOO(1),
    BAR(2)

    ;

    private final int code;

    EnumFoo(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
