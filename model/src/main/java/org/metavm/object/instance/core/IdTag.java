package org.metavm.object.instance.core;

import org.metavm.util.Utils;

public enum IdTag {

    NULL(0),
    PHYSICAL(1),
    TMP(19),
    MOCK(100);

    private final int code;

    IdTag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static IdTag fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code, () -> "Can not find IdTag for code: " + code);
    }

}
