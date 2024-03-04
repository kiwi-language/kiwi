package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "二维码入库请求项", ephemeral = false)
public record ByQrcodeInboundRequestItem(@EntityField("二维码") String qrCode) {

}
