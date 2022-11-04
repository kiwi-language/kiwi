package tech.metavm.object.meta;

import tech.metavm.util.NncUtils;

public enum ConstraintKind {
    UNIQUE(1),
    CHECK(2)
    ;

    private final int code;

    ConstraintKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ConstraintKind getByCode(int code) {
        return NncUtils.findRequired(values(), t -> t.code == code);
    }

}
