package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.api.ValueObject;
import org.metavm.entity.*;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.IndexQueryKeyItemDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.IndexField;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexQueryKeyItem extends Entity implements ValueObject {

    public static IndexQueryKeyItem create(IndexQueryKeyItemDTO itemDTO, IEntityContext context, ParsingContext parsingContext) {
        return new IndexQueryKeyItem(
                requireNonNull(context.getEntity(IndexField.class, Id.parse(itemDTO.indexFieldId()))),
                ValueFactory.create(itemDTO.value(), parsingContext)
        );
    }

    private final IndexField indexField;
    private final Value value;

    public IndexQueryKeyItem(@NotNull IndexField indexField, @NotNull Value value) {
        this.indexField = indexField;
        this.value = value;
    }

    public IndexField getIndexField() {
        return indexField;
    }

    public Value getValue() {
        return value;
    }

    public IndexQueryKeyItemDTO toDTO(SerializeContext serializeContext) {
        return new IndexQueryKeyItemDTO(serializeContext.getStringId(indexField), value.toDTO());
    }

    public String getText() {
        return indexField.getName() + ":" + value.getText();
    }
}
