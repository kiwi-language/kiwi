package tech.metavm.application;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType
public enum LabAppInvitationState {

    @EntityField("等待接受")
    INITIAL(0),
    @EntityField("已接受")
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
