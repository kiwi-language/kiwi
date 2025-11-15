package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.wire.Wire;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.wire.SubType;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.InternalException;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire(subTypes = {
        @SubType(value = 1, type = ExpressionPlaceholder.class),
        @SubType(value = 2, type = StaticPropertyExpression.class),
        @SubType(value = 3, type = FunctionExpression.class),
        @SubType(value = 4, type = ThisExpression.class),
        @SubType(value = 5, type = ConditionalExpression.class),
        @SubType(value = 6, type = InstanceOfExpression.class),
        @SubType(value = 8, type = VariablePathExpression.class),
        @SubType(value = 9, type = BinaryExpression.class),
        @SubType(value = 10, type = PropertyExpression.class),
        @SubType(value = 11, type = ArrayAccessExpression.class),
        @SubType(value = 12, type = ConstantExpression.class),
        @SubType(value = 14, type = NeverExpression.class),
        @SubType(value = 15, type = UnaryExpression.class),
        @SubType(value = 16, type = NodeExpression.class),
        @SubType(value = 18, type = AsExpression.class),
        @SubType(value = 19, type = VariableExpression.class),
        @SubType(value = 20, type = ArrayExpression.class)
})
@Entity
public abstract class Expression implements ValueObject, Element {

    protected static final int TYPE_ArrayAccessExpression = 1;
    protected static final int TYPE_ArrayExpression = 2;
    protected static final int TYPE_FunctionExpression = 3;
    protected static final int TYPE_PropertyExpression = 4;
    protected static final int TYPE_InstanceOfExpression = 5;
    protected static final int TYPE_UnaryExpression = 7;
    protected static final int TYPE_AsExpression = 8;
    protected static final int TYPE_ExpressionPlaceholder = 9;
    protected static final int TYPE_StaticPropertyExpression = 10;
    protected static final int TYPE_NeverExpression = 11;
    protected static final int TYPE_ConstantExpression = 12;
    protected static final int TYPE_ConditionalExpression = 13;
    protected static final int TYPE_ThisExpression = 14;
    protected static final int TYPE_NodeExpression = 15;
    protected static final int TYPE_BinaryExpression = 17;
    protected static final int TYPE_VariablePathExpression = 18;
    protected static final int TYPE_VariableExpression = 20;

    @Generated
    public static Expression read(MvInput input) {
        var type = input.read();
        return switch (type) {
            case TYPE_StaticPropertyExpression -> StaticPropertyExpression.read(input);
            case TYPE_VariablePathExpression -> VariablePathExpression.read(input);
            case TYPE_ExpressionPlaceholder -> ExpressionPlaceholder.read(input);
            case TYPE_ConditionalExpression -> ConditionalExpression.read(input);
            case TYPE_ArrayAccessExpression -> ArrayAccessExpression.read(input);
            case TYPE_InstanceOfExpression -> InstanceOfExpression.read(input);
            case TYPE_VariableExpression -> VariableExpression.read(input);
            case TYPE_PropertyExpression -> PropertyExpression.read(input);
            case TYPE_FunctionExpression -> FunctionExpression.read(input);
            case TYPE_ConstantExpression -> ConstantExpression.read(input);
            case TYPE_BinaryExpression -> BinaryExpression.read(input);
            case TYPE_UnaryExpression -> UnaryExpression.read(input);
            case TYPE_NeverExpression -> NeverExpression.read(input);
            case TYPE_ArrayExpression -> ArrayExpression.read(input);
            case TYPE_ThisExpression -> ThisExpression.read(input);
            case TYPE_NodeExpression -> NodeExpression.read(input);
            case TYPE_AsExpression -> AsExpression.read(input);
            default -> throw new IllegalStateException("Unrecognized type: " + type);
        };
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        var type = visitor.visitByte();
        switch (type) {
            case TYPE_StaticPropertyExpression -> StaticPropertyExpression.visit(visitor);
            case TYPE_VariablePathExpression -> VariablePathExpression.visit(visitor);
            case TYPE_ExpressionPlaceholder -> ExpressionPlaceholder.visit(visitor);
            case TYPE_ConditionalExpression -> ConditionalExpression.visit(visitor);
            case TYPE_ArrayAccessExpression -> ArrayAccessExpression.visit(visitor);
            case TYPE_InstanceOfExpression -> InstanceOfExpression.visit(visitor);
            case TYPE_VariableExpression -> VariableExpression.visit(visitor);
            case TYPE_PropertyExpression -> PropertyExpression.visit(visitor);
            case TYPE_FunctionExpression -> FunctionExpression.visit(visitor);
            case TYPE_ConstantExpression -> ConstantExpression.visit(visitor);
            case TYPE_BinaryExpression -> BinaryExpression.visit(visitor);
            case TYPE_UnaryExpression -> UnaryExpression.visit(visitor);
            case TYPE_NeverExpression -> NeverExpression.visit(visitor);
            case TYPE_ArrayExpression -> ArrayExpression.visit(visitor);
            case TYPE_ThisExpression -> ThisExpression.visit(visitor);
            case TYPE_NodeExpression -> NodeExpression.visit(visitor);
            case TYPE_AsExpression -> AsExpression.visit(visitor);
            default -> throw new IllegalStateException("Unrecognized type: " + type);
        }
    }


    protected abstract String buildSelf(VarType symbolType, boolean relaxedCheck);

    public abstract int precedence();

    public final String build(VarType symbolType) {
        return build(symbolType, false, false);
    }

    public final String build(VarType symbolType, boolean relaxedCheck) {
        return build(symbolType, false, relaxedCheck);
    }

    protected final String build(VarType symbolType, boolean withParenthesis, boolean relaxedCheck) {
        String expr = buildSelf(symbolType, relaxedCheck);
        return withParenthesis ? "(" + expr + ")" : expr;
    }

    public abstract Type getType();

    public abstract List<Expression> getComponents();

    public Expression getVariableComponent() {
        for (Expression c : getComponents()) {
            if (c instanceof PropertyExpression || c instanceof ThisExpression) {
                return c;
            }
        }
        throw new InternalException("Can not find a variable child in expression: " + build(VarType.NAME));
    }

    public <E extends Expression> E getComponent(Class<E> type) {
        for (Expression child : getComponents()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        throw new InternalException("Can not find a child expression of type '" + type.getName() + "'");
    }

    public ConstantExpression getConstantComponent() {
        return getComponent(ConstantExpression.class);
    }

    public PropertyExpression getFieldComponent() {
        return getComponent(PropertyExpression.class);
    }

    public ArrayExpression getArrayComponent() {
        return getComponent(ArrayExpression.class);
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public static String idVarName(Id id) {
        return "$" + id.toString();
    }

    public <T extends Expression> List<T> extractExpressions(Class<T> klass) {
        List<T> results = new ArrayList<>();
        if (klass.isInstance(this)) {
            results.add(klass.cast(this));
        }
        results.addAll(extractExpressionsRecursively(klass));
        return results;
    }

    public Value evaluate(EvaluationContext context) {
        if (context.isContextExpression(this))
            return context.evaluate(this);
        else
            return evaluateSelf(context);
    }

    protected abstract Value evaluateSelf(EvaluationContext context);

    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return List.of();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    @Generated
    public void write(MvOutput output) {
    }

    public abstract Expression transform(ExpressionTransformer transformer);

    public Expression copy() {
        return transform(new ExpressionTransformer() {

            @Override
            public Expression visitExpression(Expression expression) {
                return expression.copy();
            }
        });
    }

}
