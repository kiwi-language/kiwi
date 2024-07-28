package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.util.NncUtils;

@EntityType
public enum MetadataState {
    INITIALIZING(0),
    READY(1),
    ERROR(2),
    REMOVING(9),
    REMOVED(10);

    private final int code;

    MetadataState(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static MetadataState fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

}
