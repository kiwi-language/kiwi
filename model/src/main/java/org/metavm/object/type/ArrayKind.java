package org.metavm.object.type;

import org.metavm.flow.Flow;
import org.metavm.util.Utils;

import javax.annotation.Nullable;

public enum ArrayKind {

    DEFAULT(1, TypeCategory.ARRAY, TypeTags.ARRAY, "[]") {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType) {
             return (that == DEFAULT) && assignedElementType.contains(assignmentElementType);
        }

        @Override
        public String getInternalName(String elementInternalName) {
            return  elementInternalName + "[]";
        }

    },
    READ_ONLY(2, TypeCategory.READ_ONLY_ARRAY, TypeTags.READONLY_ARRAY, "[R]") {
        @Override
        public boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType) {
            return assignedElementType.isAssignableFrom(assignmentElementType);
        }

        @Override
        public String getInternalName(String elementInternalName) {
            return elementInternalName + "[R]";
        }
    },
    ;

    private final int code;
    private final TypeCategory category;
    private final int typeTag;
    private final String suffix;

    ArrayKind(int code, TypeCategory category, int typeTag, String suffix) {
        this.code = code;
        this.category = category;
        this.typeTag = typeTag;
        this.suffix = suffix;
    }

    public static ArrayKind fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }

    public TypeCategory category() {
        return category;
    }

    public int typeTag() {
        return typeTag;
    }

    public abstract boolean isAssignableFrom(ArrayKind that, Type assignedElementType, Type assignmentElementType);

    public abstract String getInternalName(String elementInternalName);

    public String getInternalName(Type elementType, @Nullable Flow current) {
        return getInternalName(elementType.getInternalName(current));
    }

    public String getSuffix() {
        return suffix;
    }
}
