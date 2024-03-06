package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "扫码入库请求", ephemeral = true)
public class ByQrCodeInboundRequest extends InboundRequest {

    @EntityField("数量")
    private long amount;

    @ChildEntity("二维码")
    private ChildList<ByQrcodeInboundRequestItem> byQrcodeItems;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public ChildList<ByQrcodeInboundRequestItem> getByQrcodeItems() {
        return byQrcodeItems;
    }

    public void setByQrcodeItems(ChildList<ByQrcodeInboundRequestItem> byQrcodeItems) {
        this.byQrcodeItems = byQrcodeItems;
    }
}
