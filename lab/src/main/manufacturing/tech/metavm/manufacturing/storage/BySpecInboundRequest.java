package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.ValueStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@ValueStruct
public class BySpecInboundRequest extends InboundRequest {

    @ChildEntity
    private ChildList<BySpecInboundRequestItem> bySpecItems;

    public BySpecInboundRequest(InboundBizType bizType, Position position, Material material, @Nullable Batch batch, @Nullable Supplier supplier, @Nullable String supplierBatchNo, @Nullable Client client, @Nullable Date arrivalDate, @Nullable Date productionDate, @Nullable Date expirationDate, Unit unit, ChildList<BySpecInboundRequestItem> bySpecItems) {
        super(bizType, position, material, batch, supplier, supplierBatchNo, client, arrivalDate, productionDate, expirationDate, unit);
        this.bySpecItems = bySpecItems;
    }

    public ChildList<BySpecInboundRequestItem> getBySpecItems() {
        return bySpecItems;
    }

    public void setBySpecItems(ChildList<BySpecInboundRequestItem> bySpecItems) {
        this.bySpecItems = bySpecItems;
    }
}
