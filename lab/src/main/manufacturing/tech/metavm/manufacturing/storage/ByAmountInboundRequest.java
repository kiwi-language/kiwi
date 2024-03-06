package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "数量入库请求", ephemeral = true)
public class ByAmountInboundRequest extends InboundRequest {

    @EntityField("数量")
    private long amount;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

}
