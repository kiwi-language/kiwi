package tech.metavm.application;

import tech.metavm.entity.EntityType;

@EntityType
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
