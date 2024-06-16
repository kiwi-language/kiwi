package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.ParsingContext;
import org.metavm.flow.rest.SearchIndexFieldDTO;
import org.metavm.object.type.IndexField;

@EntityType
public class SearchIndexField extends Entity {

    public static SearchIndexField create(SearchIndexFieldDTO fieldDTO, ParsingContext parsingContext, IEntityContext entityContext) {
        return new SearchIndexField(
                entityContext.getEntity(IndexField.class, fieldDTO.indexFieldId()),
                IndexOperator.getByCode(fieldDTO.operator()),
                ValueFactory.create(fieldDTO.value(), parsingContext)
        );
    }

    private final IndexField field;

    private final IndexOperator operator;

    private Value value;

    public SearchIndexField(IndexField field, IndexOperator operator, Value value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }


    public IndexOperator getOperator() {
        return operator;
    }

    public IndexField getField() {
        return field;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public InstanceIndexQueryItem buildQueryItem(EvaluationContext evaluationContext) {
        return new InstanceIndexQueryItem(
                field,
                operator,
                value.evaluate(evaluationContext)
        );
    }

    public SearchIndexFieldDTO toDTO(IEntityContext context) {
        try(var serContext = SerializeContext.enter()) {
            return new SearchIndexFieldDTO(
                    serContext.getStringId(field), operator.code(),
                    value.toDTO()
            );
        }
    }
}
