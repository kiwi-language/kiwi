package org.metavm.autograph.mocks;

import org.metavm.api.Entity;
import org.metavm.util.Utils;

@Entity
public enum AstProductState {

    NORMAL(0),

    OFF_THE_SHELF(1)
    ;

    private final int code;

    AstProductState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static AstProductState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

}
