package org.metavm.message;

import org.metavm.entity.EntityType;

@EntityType
public enum LabMessageKind {
    DEFAULT(0),
    INVITATION(1),
    LEAVE(2),
    ;

    final int code;

    LabMessageKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

}
