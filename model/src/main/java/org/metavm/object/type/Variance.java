package org.metavm.object.type;

import org.metavm.api.EntityType;

@EntityType
public enum Variance {
    INVARIANT,
    COVARIANT,
    CONTRAVARIANT,
}
