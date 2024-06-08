package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct(ephemeral = true)
public class ByAmountInboundRequest extends InboundRequest {

    private long amount;

    public ByAmountInboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit, long amount) {
        super(bizType, position, material, batch, supplier, supplierBatchNo, client, arrivalDate, productionDate, expirationDate, unit);
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

}
