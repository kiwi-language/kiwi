package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType("方法表达式")
public class MethodExpression extends Expression {

    @ChildEntity("实例")
    private final Expression self;

    @EntityField("方法")
    private final Method method;

    public MethodExpression(@NotNull Expression self, @NotNull Method method) {
        this.self = addChild(self.copy(), "self");
        this.method = method;
    }

    public Expression getSelf() {
        return self;
    }

    public Flow getMethod() {
        return method;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return self.buildSelf(symbolType, relaxedCheck) + "." + method.getName()
                + "(" + NncUtils.join(method.getParameters(), Parameter::getName, ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return method.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(self);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return ((ClassInstance) self.evaluate(context)).getFunction(method, context.parameterizedFlowProvider());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFuncExpression(this);
    }
}
