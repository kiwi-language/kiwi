package tech.metavm.user;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("会话状态")
public enum LabSessionState {

    @EnumConstant("正常")
    ACTIVE,

    @EnumConstant("结束")
    CLOSED,


}
