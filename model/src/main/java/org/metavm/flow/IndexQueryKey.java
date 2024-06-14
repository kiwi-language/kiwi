package org.metavm.flow;

import org.metavm.entity.Value;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.IndexQueryKeyDTO;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.util.NncUtils;

import java.util.List;

@EntityType
public class IndexQueryKey extends Entity implements Value {

    public static IndexQueryKey create(IndexQueryKeyDTO indexQueryKeyDTO, IEntityContext context, ParsingContext parsingContext) {
        return new IndexQueryKey(
                context.getEntity(Index.class, Id.parse(indexQueryKeyDTO.indexId())),
                NncUtils.map(indexQueryKeyDTO.items(), item -> IndexQueryKeyItem.create(item, context, parsingContext))
        );
    }

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
