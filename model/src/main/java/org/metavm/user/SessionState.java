package org.metavm.user;

import org.metavm.util.Utils;

public enum SessionState {

    ACTIVE(1),

    CLOSED(2),
    ;

    private final int code;

    SessionState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static SessionState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
