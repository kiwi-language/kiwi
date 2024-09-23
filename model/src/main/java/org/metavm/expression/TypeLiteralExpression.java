package org.metavm.expression;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.util.ContextUtil;

import java.util.List;

@Slf4j
public class TypeLiteralExpression extends Expression {

    private final Type type;

    public TypeLiteralExpression(Type type) {
        this.type = type;
        log.debug("Creating type literal expression for type: {}", type.getTypeDesc());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeLiteralExpression(this);
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            return type.toExpression(serContext) + ".class";
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return StdKlass.valueList.get().getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        var klass = ((ClassType) type).resolve();
        return ContextUtil.getEntityContext().getInstance(klass.getEffectiveTemplate()).getReference();
    }
}
