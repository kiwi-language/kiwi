package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.VarType;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

@EntityType
public abstract class Value extends Element implements org.metavm.api.Value {

    protected final ValueKind kind;

    protected final Expression expression;

    public Value(ValueKind kind, Expression expression) {
        this(kind, expression, null);
    }

    public Value(ValueKind kind, Expression expression, @Nullable EntityParentRef parentRef) {
        super(null, parentRef);
        this.expression = expression;
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

    public String getText() {
        return expression.build(VarType.NAME,  true);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
