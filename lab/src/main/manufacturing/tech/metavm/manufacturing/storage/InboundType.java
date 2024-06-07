package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;

@EntityType
public enum InboundType {
    BY_AMOUNT,
    BY_QR_CODE,
    BY_SPEC,
}