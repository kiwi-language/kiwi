package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType(value = "按规格入库请求项", ephemeral = true)
public record BySpecInboundRequestItem(
        @EntityField("二维码数量") int qrCodeAmount,
        @EntityField("入库数量") long inboundAmount) {

}
