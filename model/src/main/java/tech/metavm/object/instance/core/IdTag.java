package tech.metavm.object.instance.core;

import tech.metavm.util.NncUtils;

public enum IdTag {

    NULL(0),
    DEFAULT_PHYSICAL(1),
    CLASS_TYPE_PHYSICAL(2),
    TMP(3),
    DEFAULT_VIEW(4),
    CHILD_VIEW(5),
    FIELD_VIEW(6),
    ELEMENT_VIEW(7),
    MOCK(100);

    private final int code;

    IdTag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static IdTag fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
