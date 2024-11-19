package org.metavm.object.instance.core;

import org.metavm.util.NncUtils;

public enum IdTag {

    NULL(0),
    PHYSICAL(1),
    TAGGED_PHYSICAL(2),
    TMP(19),
    MOCK(100);

    private final int code;

    IdTag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public int maskedCode(boolean isArray) {
        return code | (isArray ? 0x80 : 0);
    }

    public static IdTag fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code, () -> "Can not find IdTag for code: " + code);
    }

}
