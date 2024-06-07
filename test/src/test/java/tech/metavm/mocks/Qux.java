package tech.metavm.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

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
