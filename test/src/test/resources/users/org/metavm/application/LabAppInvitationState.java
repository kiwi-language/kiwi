package org.metavm.application;

import org.metavm.api.Entity;

@Entity
public enum LabAppInvitationState {

    INITIAL(0),
    ACCEPTED(1),

    ;

    private final int code;

    LabAppInvitationState(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

}
