package tech.metavm.message;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("消息类型")
public enum MessageKind {
    @EnumConstant("默认")
    DEFAULT(0),
    @EnumConstant("应用邀请")
    INVITATION(1),
    @EnumConstant("退出应用")
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
