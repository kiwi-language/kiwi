package tech.metavm.expression;

import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AllMatchExpression extends Expression {

//    private final CursorExpression cursor;
    private final Expression array;
    private final Expression condition;
    private final CursorExpression cursor;

    public AllMatchExpression(Expression array, Expression condition) {
        this(array, condition, null);
    }

    public AllMatchExpression(Expression array, Expression condition, CursorExpression cursor) {
//        if(!(cursor.getArray().getType() instanceof ArrayType arrayType)) {
//            throw new InternalException("array expression must has an array type");
//        }
//        if(!(arrayType.getElementType() instanceof ClassType)) {
//            throw new InternalException("Only reference array is supported for AllMatchExpression right now");
//        }
        this.array = array;
        this.condition = condition;
        this.cursor = cursor;
    }

    public Expression getArray() {
//        return cursor.getArray();
        return array;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return Token.ALL_MATCH + "(" +
                getArray().build(symbolType, false) + ", "
                + condition.build(symbolType, false)
                + ")";
    }

    public ArrayType getArrayType() {
        return (ArrayType) getArray().getType();
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Type getType() {
        return ModelDefRegistry.getType(Boolean.class);
    }

    public CursorExpression getCursor() {
        return cursor;
    }

    @Nullable
    public String getCursorAlias() {
        return array instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    @Override
    public List<Expression> getChildren() {
        return List.of(array, condition);
    }

    @Override
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, 2);
        return new AllMatchExpression(children.get(0), children.get(1), null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllMatchExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(condition, that.condition) && Objects.equals(cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, condition, cursor);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAllMatchExpression(this);
    }
}
