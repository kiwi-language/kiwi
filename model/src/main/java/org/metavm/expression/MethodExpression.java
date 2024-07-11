package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;

@EntityType
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

    public Method getMethod() {
        return methodRef.resolve();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        var method = getMethod();
        return self.buildSelf(symbolType, relaxedCheck) + "." + method.getName()
                + "(" + NncUtils.join(method.getParameters(), Parameter::getName, ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return getMethod().getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(self);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return self.evaluate(context).resolveObject().getFunction(getMethod());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFuncExpression(this);
    }
}
