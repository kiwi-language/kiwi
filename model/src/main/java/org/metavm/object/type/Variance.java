package org.metavm.object.type;

import org.metavm.entity.EntityType;
import org.metavm.entity.EnumConstant;

@EntityType
public enum Variance {
    INVARIANT,
    COVARIANT,
    CONTRAVARIANT,
}
