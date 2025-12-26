package org.metavm.application;

import org.metavm.util.Utils;

public enum ApplicationState {
    ACTIVE(1),
    REMOVING(2),
    ;

    private final int code;

    ApplicationState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ApplicationState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
