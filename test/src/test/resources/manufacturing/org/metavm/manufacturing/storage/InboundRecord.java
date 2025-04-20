package org.metavm.manufacturing.storage;

import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.manufacturing.material.*;

import javax.annotation.Nullable;
import java.util.Date;

@Entity
public record InboundRecord(
        @EntityField(asTitle = true) String inboundOrderCode,
        InboundOrder.Item inboundOrderItem,
        InboundBizType bizType,
        Material material,
        Position position,
        long quantity,
        Unit unit,
        @Nullable String qrCode,
        @Nullable Batch batch,
        @Nullable Supplier supplier,
        @Nullable String supplierBatchNo,
        @Nullable Client client,
        @Nullable Date arrivalDate,
        @Nullable Date productionDate,
        @Nullable Date expirationDate,
        Date createdAt
) {

}
