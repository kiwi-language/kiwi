package org.metavm.entity;

public enum IndexSearchOp {
    EQ(1),
    GT(2),
    GE(3),
    LT(4),
    LE(5);

    private final int code;

    IndexSearchOp(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

}
