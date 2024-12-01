package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.TypeLiteralExpression;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.ContextUtil;

public class TypeValue extends Value {

    private final Type type;

    public TypeValue(Type type) {
        this.type = type;
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        var klass = Types.getKlass(type);
        return ContextUtil.getEntityContext().getInstance(klass).getReference();
    }

    @Override
    public Type getType() {
        return StdKlass.type.get().getType();
    }

    @Override
    public String getText() {
        return type.getTypeDesc();
    }

    @Override
    public Expression getExpression() {
        return new TypeLiteralExpression(type);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeValue(this);
    }
}
