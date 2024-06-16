package org.metavm.manufacturing.storage;

import org.metavm.api.EntityType;

@EntityType
public enum InboundOrderState {
    NEW,
    ISSUED,
    INBOUND,
    CANCELLED
}
