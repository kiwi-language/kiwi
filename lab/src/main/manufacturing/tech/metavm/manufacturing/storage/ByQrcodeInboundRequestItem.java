package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ValueStruct;

@ValueStruct
public record ByQrcodeInboundRequestItem(String qrCode) {

}
