package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;

import java.util.List;

@ValueStruct
public record TransferRequestItem(
        TransferOrder.Item transferOrderItem,
        List<TransferRequestSubItem> subItems
) {
}
