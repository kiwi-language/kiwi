package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record BySpecInboundRequestItem(
        int qrCodeAmount,
        long inboundAmount) {

}
