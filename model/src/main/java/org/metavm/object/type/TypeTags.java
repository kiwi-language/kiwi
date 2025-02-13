package org.metavm.object.type;

public class TypeTags {

    public static final int DEFAULT = 0;

    public static final int READONLY_ARRAY = 1;

    public static final int ARRAY = 2;

    public static final int SYSTEM_TYPE_TAG_LIMIT = 1000000;

    public static boolean isSystemTypeTag(int typeTag) {
        return typeTag > 0 && typeTag < SYSTEM_TYPE_TAG_LIMIT;
    }

    public static boolean isNonArraySystemTypeTag(int typeTag) {
        return typeTag > 4 && typeTag < SYSTEM_TYPE_TAG_LIMIT;
    }

}
