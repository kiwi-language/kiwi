package org.metavm.manufacturing.storage;

import org.metavm.api.ValueList;
import org.metavm.api.ValueStruct;

@ValueStruct
public record TransferRequest(
    Position to,
    ValueList<TransferRequestItem> items
) {
}
