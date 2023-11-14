package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;

@EntityType(compiled = true, value = "分支测试")
public class AstBranchFoo extends Entity {

    @EntityField("状态")
    private AstProductState state = AstProductState.NORMAL;
    @EntityField("库存")
    private int inventory = 0;

    @EntityFlow("扣减库存")
    public void dec(int amount) {
        if(state != AstProductState.NORMAL || this.inventory < amount)
            throw new RuntimeException("商品未上架或库存不足");
        this.inventory -= amount;
    }

    @EntityFlow("上架")
    public void putOnShelf() {
        state = AstProductState.NORMAL;
    }

    @EntityFlow("下架")
    public void takeOffShelf() {
        state = AstProductState.OFF_THE_SHELF;
    }

}
