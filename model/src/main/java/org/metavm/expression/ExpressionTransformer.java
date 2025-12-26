package org.metavm.expression;

import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;

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
