package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;

@EntityType
public enum TransferOrderStatus {
    PENDING,
    ISSUED,
    ISSUE_FAILED,
    FINISHED,
}
