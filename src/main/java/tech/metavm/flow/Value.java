package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;

import javax.annotation.Nullable;

@EntityType("流程值")
public abstract class Value extends Entity {

    @EntityField("类别")
    private final ValueKind kind;

    public Value(ValueKind kind) {
        this.kind = kind;
    }

    protected abstract FieldValue getDTOValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            if(context.isIncludingValueType()) {
                context.writeType(getType());
            }
            return new ValueDTO(kind.code(), getDTOValue(persisting));
        }
    }

    public ValueKind getKind() {
        return kind;
    }

    public abstract Type getType();

    public Instance evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(getExpression(), context);
    }

    public Instance evaluateChild(InstanceParentRef parentRef, EvaluationContext context) {
        return ExpressionEvaluator.evaluateChild(getExpression(), parentRef, context);
    }

    public abstract Expression getExpression();

    public abstract Value copy();

}
