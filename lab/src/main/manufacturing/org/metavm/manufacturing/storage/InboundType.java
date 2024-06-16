package org.metavm.manufacturing.storage;

import org.metavm.api.EntityType;

@EntityType
public enum InboundType {
    BY_AMOUNT,
    BY_QR_CODE,
    BY_SPEC,
}