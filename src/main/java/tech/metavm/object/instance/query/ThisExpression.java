package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Type;

public class ThisExpression extends Expression {

    private final Type type;

    public ThisExpression(Type type) {
        super(type.getContext());
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
