package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;

import javax.annotation.Nullable;

@EntityType("流程值")
public abstract class Value extends Element {

    @EntityField("类别")
    private final ValueKind kind;

    public Value(ValueKind kind) {
        this(kind, null);
    }

    public Value(ValueKind kind, @Nullable EntityParentRef parentRef) {
        super(null, parentRef);
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

    public abstract Value substituteExpression(Expression expression);

}
