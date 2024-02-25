package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("物料类型")
public enum MaterialKind {

    @EnumConstant("默认")
    DEFAULT,
    @EnumConstant("虚拟件")
    VIRTUAL,
    @EnumConstant("在制品")
    WORK_IN_PROCESS,

}
