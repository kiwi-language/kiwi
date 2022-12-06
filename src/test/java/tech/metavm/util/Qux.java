package tech.metavm.util;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("量子X")
public class Qux extends Entity {

    @EntityField("数量")
    private final long amount;

    public Qux(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }
}
