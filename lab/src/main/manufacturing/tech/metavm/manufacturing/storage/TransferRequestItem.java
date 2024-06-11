package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueList;
import tech.metavm.entity.ValueStruct;

@ValueStruct
public record TransferRequestItem(
        TransferOrderItem transferOrderItem,
        ValueList<TransferRequestSubItem> subItems
) {
}
