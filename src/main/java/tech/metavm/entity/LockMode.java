package tech.metavm.entity;

public enum LockMode {
    NONE(0),
    SHARE(1),
    EXCLUSIVE(2)

    ;

    private final int code;

    LockMode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
