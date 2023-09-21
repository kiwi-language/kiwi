package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType("TypeReducerFoo")
public class TypeReducerFoo extends Entity {

    @Nullable
    @EntityField("code")
    public String code;

    @EntityField("amount")
    public int amount;

}
