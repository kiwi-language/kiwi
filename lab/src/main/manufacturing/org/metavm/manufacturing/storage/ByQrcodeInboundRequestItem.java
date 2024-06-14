package org.metavm.manufacturing.storage;

import org.metavm.entity.ValueStruct;

@ValueStruct
public record ByQrcodeInboundRequestItem(String qrCode) {

}
