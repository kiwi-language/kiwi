package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.util.Utils;

@Entity
public enum ClassTypeState {
    INIT(1),
    DEPLOYED(2),

    ;

    private final int code;

    ClassTypeState(int code) {
        this.code = code;
    }

    public static ClassTypeState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public int code() {
        return code;
    }
}
