package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionEvaluator;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.InstanceParentRef;

import javax.annotation.Nullable;

@EntityType("流程值")
public abstract class Value extends Element {

    public static Value constant(Expression expression) {
        return new ConstantValue(ValueKind.CONSTANT, expression);
    }

    public static Value reference(Expression expression) {
        return new DynamicValue(ValueKind.REFERENCE, expression);
    }

    public static Value expression(Expression expression) {
        return new DynamicValue(ValueKind.EXPRESSION, expression);
    }

    public static Value nullValue() {
        return new ConstantValue(ValueKind.NULL, ExpressionUtil.nullExpression());
    }

    @EntityField("类别")
    protected final ValueKind kind;

    @ChildEntity("表达式")
    protected final Expression expression;

    public Value(ValueKind kind, Expression expression) {
        this(kind, expression, null);
    }

    public Value(ValueKind kind, Expression expression, @Nullable EntityParentRef parentRef) {
        super(null, parentRef);
        this.expression = addChild(expression.copy(), "expression");
        this.kind = kind;
    }

    protected abstract FieldValue toFieldValue(boolean persisting);

    public ValueDTO toDTO(boolean persisting) {
        try (var context = SerializeContext.enter()) {
            if(context.isIncludingValueType()) {
                context.writeType(getType());
            }
            return new ValueDTO(kind.code(), toFieldValue(persisting));
        }
    }

    public ValueKind getKind() {
        return kind;
    }

    public Type getType() {
        return expression.getType();
    }

    public Instance evaluate(EvaluationContext context) {
        return ExpressionEvaluator.evaluate(getExpression(), context);
    }

    public Instance evaluateChild(InstanceParentRef parentRef, EvaluationContext context) {
        return ExpressionEvaluator.evaluateChild(getExpression(), parentRef, context);
    }

    public Expression getExpression() {
        return expression;
    }

    public abstract Value copy();

    public abstract Value substituteExpression(Expression expression);

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
