package tech.metavm.flow;

import tech.metavm.util.NncUtils;

public enum RaiseParameterKind {

    MESSAGE(1),
    THROWABLE(2),
    ;

    private final int code;

    RaiseParameterKind(int code) {
        this.code = code;
    }

    public static RaiseParameterKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public int getCode() {
        return code;
    }
}
