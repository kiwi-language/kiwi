package org.metavm.manufacturing.storage;

import org.metavm.api.ValueList;
import org.metavm.api.ValueStruct;
import org.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@ValueStruct
public class ByQrCodeInboundRequest extends InboundRequest {

    private final long amount;

    private final ValueList<ByQrcodeInboundRequestItem> byQrcodeItems;

    public ByQrCodeInboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit, long amount, ValueList<ByQrcodeInboundRequestItem> byQrcodeItems) {
        super(bizType, position, material, batch, supplier, supplierBatchNo, client, arrivalDate, productionDate, expirationDate, unit);
        this.amount = amount;
        this.byQrcodeItems = byQrcodeItems;
    }

    public long getAmount() {
        return amount;
    }

    public ValueList<ByQrcodeInboundRequestItem> getByQrcodeItems() {
        return byQrcodeItems;
    }

}
