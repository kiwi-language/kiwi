package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("类型分类")
public enum LabTypeCategory {

    @EnumConstant("类")
    CLASS,
    @EnumConstant("枚举")
    ENUM


}
