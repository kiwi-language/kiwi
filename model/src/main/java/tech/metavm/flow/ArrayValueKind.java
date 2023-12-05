package tech.metavm.flow;

import tech.metavm.util.NncUtils;

public enum ArrayValueKind {

    ELEMENTS(1),

    ARRAY(2)

    ;

    final int code;

    ArrayValueKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ArrayValueKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }
}
