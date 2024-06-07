package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;

@EntityType
public enum Variance {
    INVARIANT,
    COVARIANT,
    CONTRAVARIANT,
}
