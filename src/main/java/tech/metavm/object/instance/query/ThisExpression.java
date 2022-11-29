package tech.metavm.object.instance.query;

import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;

@ValueType("当前对象表达式")
public class ThisExpression extends Expression {

    private final Type type;

    public ThisExpression(Type type) {
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
    public Type getType() {
        return type;
    }
}
