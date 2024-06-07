package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.IndexQueryKeyItemDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.IndexField;

import static java.util.Objects.requireNonNull;

@EntityType
public class IndexQueryKeyItem extends Entity implements tech.metavm.entity.Value {

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
