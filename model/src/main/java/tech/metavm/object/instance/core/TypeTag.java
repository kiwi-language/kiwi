package tech.metavm.object.instance.core;

import tech.metavm.entity.EntityType;
import tech.metavm.object.type.TypeCategory;

@EntityType("类型标签")
public enum TypeTag {
    CLASS(1),
    ARRAY(2),

    ;

    private final int code;

    TypeTag(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static TypeTag fromCode(int code) {
        return switch (code) {
            case 1 -> CLASS;
            case 2 -> ARRAY;
            default -> throw new IllegalArgumentException("Unknown code: " + code);
        };
    }

    public static TypeTag fromCategory(TypeCategory category) {
        if(category.isPojo())
            return CLASS;
        if(category.isArray())
            return ARRAY;
        throw new IllegalArgumentException("Unknown category: " + category);
    }

}
