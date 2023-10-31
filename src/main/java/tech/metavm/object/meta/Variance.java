package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType("变型")
public enum Variance {
    @EnumConstant("不变")
    INVARIANT,
    @EnumConstant("协变")
    COVARIANT,
    @EnumConstant("逆变")
    CONTRAVARIANT,
}
