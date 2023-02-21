package tech.metavm.object.instance.query;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

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
    protected List<Expression> getChildren() {
        return List.of(array, condition);
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        NncUtils.requireLength(children, 2);
        return new AllMatchExpression(children.get(0), children.get(1), null);
    }
}
