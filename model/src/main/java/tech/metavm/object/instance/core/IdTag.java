package tech.metavm.object.instance.core;

import tech.metavm.util.NncUtils;

public enum IdTag {

    NULL(0),
    OBJECT_PHYSICAL(1),
    CLASS_TYPE_PHYSICAL(3),
    ARRAY_TYPE_PHYSICAL(4),
    FIELD_PHYSICAL(5),
    TMP(19),
    DEFAULT_VIEW(11),
    CHILD_VIEW(13),
    FIELD_VIEW(15),
    ELEMENT_VIEW(17),
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
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
