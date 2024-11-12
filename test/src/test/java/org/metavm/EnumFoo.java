package org.metavm;

public enum EnumFoo {

    e1(1),
    e2(e1.code + 1)

    ;

    private final int code;

    EnumFoo(int code) {
        this.code = code;
    }
}
