package org.metavm.manufacturing.storage;

import org.metavm.entity.EntityType;

@EntityType
public enum InboundOrderState {
    NEW,
    ISSUED,
    INBOUND,
    CANCELLED
}
