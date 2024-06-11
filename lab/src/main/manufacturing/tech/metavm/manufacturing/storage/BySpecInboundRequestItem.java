package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueStruct;

@ValueStruct
public record BySpecInboundRequestItem(
        int qrCodeAmount,
        long inboundAmount) {

}
