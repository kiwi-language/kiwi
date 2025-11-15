package org.metavm.expression;

import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import java.util.LinkedList;
import org.metavm.util.Utils;

import javax.annotation.Nullable;

public class ExpressionResolverV2 extends ExpressionTransformer {

    private final LinkedList<Type> assignedTypeStack = new LinkedList<>();

    public static Expression resolve(Expression expression, @Nullable Type assignedType, ParsingContext parsingContext) {
        return expression.accept(new ExpressionResolverV2(parsingContext, assignedType));
    }

    private final ParsingContext context;

    private ExpressionResolverV2(ParsingContext context, @Nullable Type assignedType) {
        assignedTypeStack.push(assignedType);
        this.context = context;
    }

    @Override
    public Expression visitExpression(Expression expression) {
        assignedTypeStack.push(null);
        try {
            return expression.transform(this);
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Expression visitVariableExpression(VariableExpression expr) {
        Var variable = Var.parse(expr.getVariable());
        if (context.isContextVar(variable)) {
            return context.resolveVar(variable);
        } else {
            try {
                var qualifier = context.getDefaultExpr();
                var qualifierType = (ClassType) qualifier.getType();
                var propertyRef = qualifierType.getPropertyByVar(Var.parse(expr.getVariable()));
                return new PropertyExpression(qualifier, propertyRef);
            } catch (InternalException e) {
                throw new InternalException("Fail to resolve variable: " + expr.getVariable(), e);
            }
        }
    }

    @Override
    public Expression visitArrayExpression(ArrayExpression array) {
        try {
            var assignedType = assignedTypeStack.peek();
            var assignedElementType = assignedType instanceof ArrayType arrayType ? arrayType.getElementType() : null;
            assignedTypeStack.push(assignedElementType);
            var elements = Utils.map(array.getExpressions(), e -> e.accept(this));
            var types = Utils.map(elements, Expression::getType);
            Type elementType = assignedElementType;
            if (elementType == null) {
                if (types.isEmpty()) {
                    elementType = Types.getNeverType();
                } else {
                    elementType = Types.getLeastUpperBound(types);
                }
            }
            return new ArrayExpression(
                    elements,
                    new ArrayType(elementType, ArrayKind.DEFAULT)
            );
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Expression visitConditionalExpression(ConditionalExpression expression) {
        assignedTypeStack.push(null);
        try {
            return ConditionalExpression.create(
                    expression.getCondition().accept(this),
                    expression.getTrueValue().accept(this),
                    expression.getFalseValue().accept(this)
            );
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Expression visitVariablePathExpression(VariablePathExpression expression) {
        try {
            assignedTypeStack.push(null);
            if (expression.getQualifier() instanceof VariableExpression qualifierVariableExpr) {
                Var qualifierVar = Var.parse(qualifierVariableExpr.getVariable());
                if (!context.isContextVar(qualifierVar) && context.getTypeDefProvider() != null) {
                    Klass type = getClassType(qualifierVar, context.getTypeDefProvider());
                    if (type != null) {

                        return new StaticPropertyExpression(
                                type.getStaticPropertyByVar(Var.parse(expression.getField().getVariable())).getRef()
                        );
                    }
                }
            }
            var qualifier = expression.getQualifier().accept(this);
            var qualifierType = (ClassType) context.getExpressionType(qualifier).getUnderlyingType();
            var propertyRef = qualifierType.getPropertyByVar(Var.parse(expression.getField().getVariable()));
            if (propertyRef == null)
                throw new InternalException("Property not found: " + expression.getField().getVariable() + " in type " + qualifierType);
            return new PropertyExpression(qualifier, propertyRef);
        } finally {
            assignedTypeStack.pop();
        }
    }

    private Klass getClassType(Var var, IndexedTypeDefProvider klassProvider) {
        return switch (var.getType()) {
            case ID -> klassProvider.getTypeDef(var.getId()) instanceof Klass classType ? classType : null;
            case NAME -> klassProvider.findKlassByName(var.getName());
        };
    }

}
