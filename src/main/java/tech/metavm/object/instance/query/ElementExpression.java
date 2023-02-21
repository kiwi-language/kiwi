package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ElementExpression extends Expression {

    private final Expression array;
    private final int index;

    public ElementExpression(Expression array, int index) {
        this.array = array;
        this.index = index;
        NncUtils.requirePositive(index);
        if(!array.getType().getUnderlyingType().isArray()) {
            throw new InternalException("Array expression must have an array type");
        }
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return array.buildSelf(symbolType) + "[" + index + "]";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return  ((ArrayType) array.getType().getUnderlyingType()).getElementType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of(array);
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        NncUtils.requireLength(children, 1);
        return new ElementExpression(array, index);
    }
}
