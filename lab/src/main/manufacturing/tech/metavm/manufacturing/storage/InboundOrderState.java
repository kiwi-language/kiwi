package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;

@EntityType
public enum InboundOrderState {
    NEW,
    ISSUED,
    INBOUND,
    CANCELLED
}
