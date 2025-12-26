package org.metavm.object.type;

import org.metavm.util.Utils;

public enum ClassTypeState {
    INIT(1),
    DEPLOYED(2),
    REMOVING(3),

    ;

    private final int code;

    ClassTypeState(int code) {
        this.code = code;
    }

    public static ClassTypeState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }
}
