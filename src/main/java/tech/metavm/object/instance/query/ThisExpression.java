package tech.metavm.object.instance.query;

import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.ClassType;

@ValueType("当前对象表达式")
public class ThisExpression extends Expression {

    private final ClassType type;

    public ThisExpression(ClassType type) {
//        super(type.getInstanceContext());
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
