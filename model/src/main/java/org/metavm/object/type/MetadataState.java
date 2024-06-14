package org.metavm.object.type;

import org.metavm.entity.EntityType;

@EntityType
public enum MetadataState {
    INITIALIZING(0),
    READY(1),
    ERROR(2),
    REMOVING(9);

    private final int code;

    MetadataState(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

}
