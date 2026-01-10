package org.manul.expression;

import org.manul.entity.Element;
import org.manul.entity.ElementVisitor;

public class ExpressionTransformer extends ElementVisitor<Expression> {

    @Override
    public Expression visitElement(Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression visitExpression(Expression expression) {
        return expression.transform(this);
    }
}
