package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueList;
import tech.metavm.entity.ValueStruct;

@ValueStruct
public record TransferRequest(
    Position to,
    ValueList<TransferRequestItem> items
) {
}
