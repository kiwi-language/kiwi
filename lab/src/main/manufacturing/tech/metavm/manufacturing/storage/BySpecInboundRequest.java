package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;
import tech.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@EntityStruct(value = "规格入库请求", ephemeral = true)
public class BySpecInboundRequest extends InboundRequest {

    @ChildEntity("规格")
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
