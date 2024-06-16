package org.metavm.manufacturing.storage;

import org.metavm.api.ValueList;
import org.metavm.api.ValueStruct;

@ValueStruct
public record TransferRequestItem(
        TransferOrderItem transferOrderItem,
        ValueList<TransferRequestSubItem> subItems
) {
}
