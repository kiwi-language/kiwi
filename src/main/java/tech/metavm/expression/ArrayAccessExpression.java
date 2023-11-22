package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityType;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType("数组访问表达式")
public class ArrayAccessExpression extends Expression {

    @ChildEntity("数组")
    private final Expression array;
    @ChildEntity("索引")
    private final Expression index;

    public ArrayAccessExpression(@NotNull Expression array, @NotNull Expression index) {
//        NncUtils.requireTrue(array.getType() instanceof ArrayType);
        this.array = addChild(array.copy(), "array");
        this.index = addChild(index.copy(), "index");
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
    public List<Expression> getChildren() {
        return List.of(array, index);
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayAccessExpression(this);
    }
}
