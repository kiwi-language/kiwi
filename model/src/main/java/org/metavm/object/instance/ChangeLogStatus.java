package org.metavm.object.instance;

import org.metavm.util.NncUtils;

public enum ChangeLogStatus {
    PENDING(0),
    COMPLETED(10);

    private final int code;

    ChangeLogStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ChangeLogStatus fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
