package org.metavm.application;

import org.metavm.api.Entity;
import org.metavm.util.Utils;

@Entity
public enum AppInvitationState {

    INITIAL(0),
    ACCEPTED(1),

    ;

    private final int code;

    AppInvitationState(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static AppInvitationState fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }
}
