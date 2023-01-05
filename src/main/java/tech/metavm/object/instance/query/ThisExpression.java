package tech.metavm.object.instance.query;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.ClassType;

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
}
