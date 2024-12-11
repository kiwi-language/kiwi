package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;

@Entity
public class Qux extends org.metavm.entity.Entity {

    public static final IndexDef<Qux> IDX_AMOUNT = new IndexDef<>(Qux.class,false, "amount");

    private final long amount;

    public Qux(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }
}
