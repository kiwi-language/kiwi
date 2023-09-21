package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.expression.Expression;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.meta.Type;

@EntityType("流程值")
public abstract class Value extends Entity {

    @EntityField("类别")
    private final ValueKind kind;

    public Value(ValueKind kind) {
        this.kind = kind;
    }

    protected abstract FieldValueDTO getDTOValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        return new ValueDTO(kind.code(), getDTOValue(persisting), persisting ? null :
                getType().toDTO()
        );
    }

    public ValueKind getKind() {
        return kind;
    }

    public abstract Type getType();

    public abstract Instance evaluate(EvaluationContext evaluationContext);

    public abstract Expression getExpression();

}
