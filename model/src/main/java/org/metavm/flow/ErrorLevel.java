package org.metavm.flow;

import org.metavm.util.Utils;

public enum ErrorLevel {
    WARNING(1),
    ERROR(2);

    private final int code;

    ErrorLevel(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ErrorLevel fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }
}
