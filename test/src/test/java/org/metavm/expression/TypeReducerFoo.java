package org.metavm.expression;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;

import javax.annotation.Nullable;

@EntityType
public class TypeReducerFoo extends Entity {

    @Nullable
    public String code;

    public int amount;

}
