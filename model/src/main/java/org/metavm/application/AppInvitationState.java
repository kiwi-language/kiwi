package org.metavm.application;

import org.metavm.api.EntityType;

@EntityType
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

}
