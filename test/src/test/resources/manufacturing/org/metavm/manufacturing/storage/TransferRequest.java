package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;

import java.util.List;

@ValueStruct
public record TransferRequest(
    Position to,
    List<TransferRequestItem> items
) {
}
