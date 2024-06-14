package org.metavm.manufacturing.storage;

import org.metavm.entity.ValueList;
import org.metavm.entity.ValueStruct;

@ValueStruct
public record TransferRequestItem(
        TransferOrderItem transferOrderItem,
        ValueList<TransferRequestSubItem> subItems
) {
}
