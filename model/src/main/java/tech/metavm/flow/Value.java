package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.Expression;
import tech.metavm.expression.VarType;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

@EntityType("流程值")
public abstract class Value extends Element {

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

    protected abstract FieldValue toFieldValue();

    public ValueDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new ValueDTO(kind.code(), toFieldValue());
        }
    }

    public ValueKind getKind() {
        return kind;
    }

    public Type getType() {
        return expression.getType();
    }

    public Instance evaluate(EvaluationContext context) {
        return expression.evaluate(context);
    }

    public Expression getExpression() {
        return expression;
    }

    public abstract Value copy();

    public String getText() {
        return expression.build(VarType.NAME,  true);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
