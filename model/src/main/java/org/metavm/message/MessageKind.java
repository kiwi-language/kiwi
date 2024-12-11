package org.metavm.message;

import org.metavm.api.Entity;

@Entity
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
