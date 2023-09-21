package tech.metavm.expression;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.ClassType;

import java.util.List;
import java.util.Objects;

@EntityType("当前对象表达式")
public class ThisExpression extends Expression {

    @EntityField("类型")
    private final ClassType type;

    public ThisExpression(ClassType type) {
        this.type = type;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return "this";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new ThisExpression(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThisExpression that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
