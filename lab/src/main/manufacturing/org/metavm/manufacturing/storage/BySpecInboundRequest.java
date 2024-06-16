package org.metavm.manufacturing.storage;

import org.metavm.api.ValueList;
import org.metavm.api.ValueStruct;
import org.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@ValueStruct
public class BySpecInboundRequest extends InboundRequest {

    private final ValueList<BySpecInboundRequestItem> bySpecItems;

    public BySpecInboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit, ValueList<BySpecInboundRequestItem> bySpecItems) {
        super(bizType, position, material, batch, supplier, supplierBatchNo, client, arrivalDate, productionDate, expirationDate, unit);
        this.bySpecItems = bySpecItems;
    }

    public ValueList<BySpecInboundRequestItem> getBySpecItems() {
        return bySpecItems;
    }

}
