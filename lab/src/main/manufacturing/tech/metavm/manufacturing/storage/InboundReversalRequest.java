package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityStruct;

@EntityStruct(value = "入库冲销请求", ephemeral = true)
public record InboundReversalRequest(
        @ChildEntity("请求项") ChildList<InboundReversalRequestItem> items
) {

}
