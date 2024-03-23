package tech.metavm.object.instance.core;

import tech.metavm.util.NncUtils;

public enum IdTag {

    NULL(0),
    DEFAULT_PHYSICAL(1),
    CLASS_TYPE_PHYSICAL(2),
    ARRAY_TYPE_PHYSICAL(3),
    FIELD_PHYSICAL(4),
    TMP(19),
    DEFAULT_VIEW(11),
    CHILD_VIEW(12),
    FIELD_VIEW(13),
    ELEMENT_VIEW(14),
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
