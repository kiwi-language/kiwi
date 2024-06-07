package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record ByQrcodeInboundRequestItem(@EntityField("二维码") String qrCode) {

}
