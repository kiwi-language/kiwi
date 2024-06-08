package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

@EntityStruct(ephemeral = true)
public record TransferRequest(
    Position to,
    @ChildEntity ChildList<TransferRequestItem> items
) {
}
