package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "规格入库请求", ephemeral = true)
public class BySpecInboundRequest extends InboundRequest {

    @ChildEntity("规格")
    private ChildList<BySpecInboundRequestItem> bySpecItems;

    public ChildList<BySpecInboundRequestItem> getBySpecItems() {
        return bySpecItems;
    }

    public void setBySpecItems(ChildList<BySpecInboundRequestItem> bySpecItems) {
        this.bySpecItems = bySpecItems;
    }
}
