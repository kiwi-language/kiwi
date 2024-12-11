package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.MethodRef;
import org.metavm.flow.ParameterRef;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;

@Entity
public class MethodExpression extends Expression {

    private final Expression self;

    private final MethodRef methodRef;

    public MethodExpression(@NotNull Expression self, @NotNull MethodRef methodRef) {
        this.self = self;
        this.methodRef = methodRef;
    }

    public Expression getSelf() {
        return self;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return self.buildSelf(symbolType, relaxedCheck) + "." + methodRef.getName()
                + "(" + NncUtils.join(methodRef.getParameters(), ParameterRef::getName, ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return methodRef.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(self);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return self.evaluate(context).resolveObject().getFunction(methodRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFuncExpression(this);
    }
}
