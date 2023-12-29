package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.SearchIndexFieldDTO;
import tech.metavm.object.type.IndexField;

import java.util.Objects;

@EntityType("查询索引字段")
public class SearchIndexField extends Entity {

    public static SearchIndexField create(SearchIndexFieldDTO fieldDTO, ParsingContext parsingContext, IEntityContext entityContext) {
        return new SearchIndexField(
                entityContext.getEntity(IndexField.class, fieldDTO.indexFieldRef()),
                IndexOperator.getByCode(fieldDTO.operator()),
                ValueFactory.create(fieldDTO.value(), parsingContext)
        );
    }

    @EntityField("索引字段")
    private final IndexField field;

    private final IndexOperator operator;

    @ChildEntity("值")
    private Value value;

    public SearchIndexField(IndexField field, IndexOperator operator, Value value) {
        this.field = field;
        this.operator = operator;
        this.value = addChild(value, "value");
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
        this.value = addChild(value, "value");
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
                    serContext.getRef(field), operator.code(),
                    value.toDTO()
            );
        }
    }
}
