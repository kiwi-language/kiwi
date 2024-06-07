package tech.metavm.message;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

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
