package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("AST商品")
public class AstProduct extends Entity {

    @EntityField("库存")
    private int inventory;

    @EntityField("状态")
    private int state;

    public boolean dec(int amount) {
        boolean valid;
        if(state == 0 && amount <= inventory) valid = true;
        else valid = false;
        if(valid) {
            inventory -= amount;
            return true;
        }
        else return false;
    }

}
