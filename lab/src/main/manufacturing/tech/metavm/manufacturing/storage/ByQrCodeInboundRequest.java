package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct(ephemeral = true)
public class ByQrCodeInboundRequest extends InboundRequest {

    private long amount;

    @ChildEntity
    private ChildList<ByQrcodeInboundRequestItem> byQrcodeItems;

    public ByQrCodeInboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit, long amount, ChildList<ByQrcodeInboundRequestItem> byQrcodeItems) {
        super(bizType, position, material, batch, supplier, supplierBatchNo, client, arrivalDate, productionDate, expirationDate, unit);
        this.amount = amount;
        this.byQrcodeItems = byQrcodeItems;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public ChildList<ByQrcodeInboundRequestItem> getByQrcodeItems() {
        return byQrcodeItems;
    }

    public void setByQrcodeItems(ChildList<ByQrcodeInboundRequestItem> byQrcodeItems) {
        this.byQrcodeItems = byQrcodeItems;
    }
}
