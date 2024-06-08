package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

@EntityType(compiled = true)
public class AstBranchFoo extends Entity {

    private AstProductState state = AstProductState.NORMAL;
    private int inventory = 0;

    public void dec(int amount) {
        if(state != AstProductState.NORMAL || this.inventory < amount)
            throw new RuntimeException("Product is out of inventory of off shelf");
        this.inventory -= amount;
    }

    public void putOnShelf() {
        state = AstProductState.NORMAL;
    }

    public void takeOffShelf() {
        state = AstProductState.OFF_THE_SHELF;
    }

}
