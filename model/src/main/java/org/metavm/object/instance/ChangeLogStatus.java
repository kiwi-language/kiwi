package org.metavm.object.instance;

import org.metavm.util.Utils;

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
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
