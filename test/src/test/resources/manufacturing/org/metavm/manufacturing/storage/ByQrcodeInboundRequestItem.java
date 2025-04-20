package org.metavm.manufacturing.storage;

import org.metavm.api.ValueStruct;

@ValueStruct
public record ByQrcodeInboundRequestItem(String qrCode) {

}
