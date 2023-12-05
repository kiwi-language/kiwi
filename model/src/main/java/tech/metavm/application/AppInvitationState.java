package tech.metavm.application;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("应用邀请状态")
public enum AppInvitationState {

    @EntityField("等待接受")
    INITIAL(0),
    @EntityField("已接受")
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
