package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;

@ValueStruct
public record BySpecInboundRequestItem(
        int qrCodeAmount,
        long inboundAmount) {

}
