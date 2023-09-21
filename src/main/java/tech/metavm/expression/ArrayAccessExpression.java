package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class ArrayAccessExpression extends Expression {

    @ChildEntity("数组")
    private final Expression array;
    @ChildEntity("索引")
    private final Expression index;

    public ArrayAccessExpression(Expression array, Expression index) {
//        NncUtils.requireTrue(array.getType() instanceof ArrayType);
        this.array = array;
        this.index = index;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return array.build(symbolType, false)
                + "[" + index.build(symbolType, false) + "]";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return ((ArrayType) array.getType()).getElementType();
    }

    @Override
    protected List<Expression> getChildren() {
        return List.of(array, index);
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new ArrayAccessExpression(children.get(0), children.get(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayAccessExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, index);
    }
}
