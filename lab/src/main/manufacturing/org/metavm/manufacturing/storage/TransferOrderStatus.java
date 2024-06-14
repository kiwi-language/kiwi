package org.metavm.manufacturing.storage;

import org.metavm.entity.EntityType;

@EntityType
public enum TransferOrderStatus {
    PENDING,
    ISSUED,
    ISSUE_FAILED,
    FINISHED,
}
