package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;

@Entity
public enum TransferOrderStatus {
    PENDING,
    ISSUED,
    ISSUE_FAILED,
    FINISHED,
}
