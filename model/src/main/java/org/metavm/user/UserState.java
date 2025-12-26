package org.metavm.user;

import org.metavm.util.Utils;

public enum UserState {
    ACTIVE(1),
    INACTIVE(2),
    DETACHED(3);

    private final int code;

    UserState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static UserState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
