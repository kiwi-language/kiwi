package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "调拨请求", ephemeral = true)
public record TransferRequest(
    @EntityField("入库位置") Position to,
    @ChildEntity("行") ChildList<TransferRequestItem> items
) {
}
