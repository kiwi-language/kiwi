package tech.metavm.expression;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType
public class TypeReducerFoo extends Entity {

    @Nullable
    public String code;

    public int amount;

}
