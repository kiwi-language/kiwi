package org.metavm.object.instance;

import org.metavm.util.Utils;

public enum ChangeType {
    INSERT(1),
    UPDATE(2),
    DELETE(3);

    private final int code;

    ChangeType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ChangeType fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
