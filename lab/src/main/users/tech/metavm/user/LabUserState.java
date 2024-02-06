package tech.metavm.user;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("用户状态")
public enum LabUserState {
    @EntityField("启用中")
    ACTIVE,
    @EntityField("未启用")
    INACTIVE,
    @EntityField("已移除")
    DETACHED
}
