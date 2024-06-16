package org.metavm.mocks;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;
import org.metavm.entity.IndexDef;

@EntityType
public class Qux extends Entity {

    public static final IndexDef<Qux> IDX_AMOUNT = new IndexDef<>(Qux.class,false, "amount");

    private final long amount;

    public Qux(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }
}
