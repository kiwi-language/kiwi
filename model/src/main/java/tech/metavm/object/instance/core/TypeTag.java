package tech.metavm.object.instance.core;

import tech.metavm.entity.EntityType;
import tech.metavm.object.type.TypeCategory;

@EntityType("类型标签")
public enum TypeTag {
    Class(1),
    Array(2),

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
            case 1 -> Class;
            case 2 -> Array;
            default -> throw new IllegalArgumentException("Unknown code: " + code);
        };
    }

    public static TypeTag fromCategory(TypeCategory category) {
        if(category.isPojo())
            return Class;
        if(category.isArray())
            return Array;
        throw new IllegalArgumentException("Unknown category: " + category);
    }

}
