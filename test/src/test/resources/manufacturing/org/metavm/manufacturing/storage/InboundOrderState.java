package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;

@Entity
public enum InboundOrderState {
    NEW,
    ISSUED,
    INBOUND,
    CANCELLED
}
