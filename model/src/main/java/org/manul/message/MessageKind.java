package org.manul.message;

import org.manul.util.Utils;

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

    public static MessageKind fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
