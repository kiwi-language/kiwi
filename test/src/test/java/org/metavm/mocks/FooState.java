package org.metavm.mocks;

import org.metavm.util.Utils;

public enum FooState {
    STATE1(1),
    STATE2(2);

    private final int code;

    FooState(int code) {
        this.code = code;
    }

    public static FooState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }
}
