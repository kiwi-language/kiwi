package org.metavm.message;

import org.metavm.entity.EntityType;
import org.metavm.entity.EnumConstant;

@EntityType
public enum MessageKind {
    DEFAULT(0),
    INVITATION(1),
    LEAVE(2),
    ;

    final int code;

    MessageKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

}
