package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "调拨请求行", ephemeral = true)
public record TransferRequestItem(
        @EntityField("调拨单行") TransferOrderItem transferOrderItem,
        @ChildEntity("子行") ChildList<TransferRequestSubItem> subItems
) {
}
