package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.Type;

import java.util.List;

@EntityType("函数对象表达式")
public class FuncExpression extends Expression {

    @ChildEntity("实例")
    private final Expression self;

    @EntityField("流程")
    private final Flow flow;

    public FuncExpression(Expression self, Flow flow) {
        this.self = self;
        this.flow = flow;
    }

    public Expression getSelf() {
        return self;
    }

    public Flow getFlow() {
        return flow;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return null;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public List<Expression> getChildren() {
        return null;
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        return null;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFuncExpression(this);
    }
}
