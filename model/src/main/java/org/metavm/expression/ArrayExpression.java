package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import java.util.*;
import java.util.function.Consumer;

@Wire
@Entity
public class ArrayExpression extends Expression {

    public static ArrayExpression create(List<Expression> expressions) {
        var type = new ArrayType(Types.getLeastUpperBound(Utils.map(expressions, Expression::getType)), ArrayKind.READ_ONLY);
        return new ArrayExpression(expressions, type);
    }

    private final List<Expression> expressions;
    private final ArrayType type;

    public ArrayExpression(Collection<Expression> expressions, ArrayType type) {
        this.expressions = new ArrayList<>(expressions);
        this.type = type;
    }

    public static ArrayExpression merge(Expression first, Expression second) {
        if (first instanceof ArrayExpression listExpression) {
            var rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            expressions.addAll(rest);
            expressions.add(second);
            return create(expressions);
        } else {
            return create(List.of(first, second));
        }
    }

    @Generated
    public static ArrayExpression read(MvInput input) {
        return new ArrayExpression(input.readList(() -> Expression.read(input)), (ArrayType) input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitList(() -> Expression.visit(visitor));
        visitor.visitValue();
    }

    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "[" + Utils.join(expressions, expr -> expr.buildSelf(symbolType, relaxedCheck), ", ") + "]";
    }

    @Override
    public int precedence() {
        return 9;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    @Override
    public List<Expression> getComponents() {
        return Utils.listOf(expressions);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return new ArrayInstance(type, Utils.map(expressions, e -> e.evaluate(context))).getReference();
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return Utils.flatMap(expressions, expr -> expr.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayExpression that)) return false;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        expressions.forEach(arg -> arg.accept(visitor));
        type.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var expressions_ : expressions) expressions_.forEachReference(action);
        type.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ArrayExpression);
        super.write(output);
        output.writeList(expressions, arg0 -> arg0.write(output));
        output.writeValue(type);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new ArrayExpression(Utils.map(expressions, e -> e.accept(transformer)), type);
    }
}
