package tech.metavm.user;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType
public enum UserState {
    @EntityField("启用中")
    ACTIVE,
    @EntityField("未启用")
    INACTIVE,
    @EntityField("已移除")
    DETACHED
}
