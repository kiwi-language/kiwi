package org.metavm.flow;

import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionParser;
import org.metavm.expression.Expressions;
import org.metavm.expression.ParsingContext;
import org.metavm.object.type.Property;

public class Values {

    public static Value constantLong(long value) {
        return constant(Expressions.constantLong(value));
    }

    public static Value constantString(String string) {
        return constant(Expressions.constantString(string));
    }

    public static Value constantBoolean(boolean bool) {
        return constant(Expressions.constantBoolean(bool));
    }

    public static Value constant(Expression expression) {
        return new ConstantValue(ValueKind.CONSTANT, expression);
    }

    public static Value constantTrue() {
        return constant(Expressions.trueExpression());
    }

    public static Value reference(Expression expression) {
        return new DynamicValue(ValueKind.REFERENCE, expression);
    }

    public static Value expression(Expression expression) {
        return new DynamicValue(ValueKind.EXPRESSION, expression);
    }

    public static Value expression(String expression, ParsingContext parsingContext) {
        return expression(ExpressionParser.parse(expression, parsingContext));
    }

    public static Value nullValue() {
        return new ConstantValue(ValueKind.NULL, Expressions.nullExpression());
    }

    public static Value node(NodeRT node) {
        return reference(Expressions.node(node));
    }

    public static Value nodeProperty(NodeRT node, Property property) {
        return reference(Expressions.nodeProperty(node, property));
    }

    public static Value inputValue(InputNode node, int parameterIndex) {
        return nodeProperty(node, node.getType().resolve().getFields().get(parameterIndex));
    }
}
