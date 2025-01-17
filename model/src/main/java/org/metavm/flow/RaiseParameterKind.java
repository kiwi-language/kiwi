package org.metavm.flow;

import org.metavm.util.Utils;

public enum RaiseParameterKind {

    MESSAGE(1),
    THROWABLE(2),
    ;

    private final int code;

    RaiseParameterKind(int code) {
        this.code = code;
    }

    public static RaiseParameterKind getByCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public int getCode() {
        return code;
    }
}
