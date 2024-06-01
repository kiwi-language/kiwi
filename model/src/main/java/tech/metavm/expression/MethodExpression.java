package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Method;
import tech.metavm.flow.MethodRef;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("方法表达式")
public class MethodExpression extends Expression {

    @EntityField("实例")
    private final Expression self;

    @EntityField("方法")
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
        return ((ClassInstance) self.evaluate(context)).getFunction(getMethod());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFuncExpression(this);
    }
}
