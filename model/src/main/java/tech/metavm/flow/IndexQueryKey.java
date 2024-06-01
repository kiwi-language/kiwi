package tech.metavm.flow;

import tech.metavm.entity.Value;
import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.IndexQueryKeyDTO;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Index;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("索引查询键")
public class IndexQueryKey extends Entity implements Value {

    public static IndexQueryKey create(IndexQueryKeyDTO indexQueryKeyDTO, IEntityContext context, ParsingContext parsingContext) {
        return new IndexQueryKey(
                context.getEntity(Index.class, Id.parse(indexQueryKeyDTO.indexId())),
                NncUtils.map(indexQueryKeyDTO.items(), item -> IndexQueryKeyItem.create(item, context, parsingContext))
        );
    }

    @EntityField("索引")
    private final Index index;

    private final ValueArray<IndexQueryKeyItem> items;

    public IndexQueryKey(Index index, List<IndexQueryKeyItem> items) {
        this.index = index;
        this.items = new ValueArray<>(IndexQueryKeyItem.class, items);
    }

    public List<IndexQueryKeyItem> getItems() {
        return items.toList();
    }

    public IndexQueryKeyDTO toDTO(SerializeContext serializeContext) {
        return new IndexQueryKeyDTO(
                serializeContext.getStringId(index),
                NncUtils.map(items, indexQueryKeyItem -> indexQueryKeyItem.toDTO(serializeContext)));
    }

    public String getText() {
        return "{" + NncUtils.join(items, IndexQueryKeyItem::getText) + "}";
    }

    public IndexKeyRT buildIndexKey(EvaluationContext evaluationContext) {
        return index.createIndexKey(
                NncUtils.map(items, item -> item.getValue().evaluate(evaluationContext))
        );
    }

}
