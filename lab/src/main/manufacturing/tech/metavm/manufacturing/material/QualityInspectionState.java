package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("质检状态")
public enum QualityInspectionState {
    @EnumConstant("合格")
    QUALIFIED,
    @EnumConstant("待质检")
    WAITING,
    @EnumConstant("不合格")
    UNQUALIFIED,
    @EnumConstant("让步合格")
    CONCESSION_ACCEPTED

}
