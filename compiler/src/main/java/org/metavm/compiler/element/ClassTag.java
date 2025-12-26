package org.metavm.compiler.element;

public enum ClassTag {
    // The order matters, don't change
    ENUM(2),
    VALUE(4),
    CLASS(1),
    INTERFACE(3),
    ;

    private final int code;

    ClassTag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
