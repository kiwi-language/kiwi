package tech.metavm.application;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("应用状态")
public enum LabApplicationState {
    @EnumConstant("正常")
    ACTIVE,
    @EnumConstant("删除中")
    REMOVING,
}
