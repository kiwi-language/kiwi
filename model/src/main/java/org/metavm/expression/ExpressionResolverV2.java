package org.metavm.expression;

import org.metavm.entity.CopyVisitor;
import org.metavm.entity.Element;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.LinkedList;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class ExpressionResolverV2 extends CopyVisitor {

    private final LinkedList<Type> assignedTypeStack = new LinkedList<>();

    public static Expression resolve(Expression expression, @Nullable Type assignedType, ParsingContext parsingContext) {
        return (Expression) expression.accept(new ExpressionResolverV2(expression, parsingContext, assignedType));
    }

    private final ParsingContext context;

    private ExpressionResolverV2(Expression root, ParsingContext context, @Nullable Type assignedType) {
        super(root, false);
        assignedTypeStack.push(assignedType);
        this.context = context;
    }

    @Override
    public Element visitExpression(Expression expression) {
        assignedTypeStack.push(null);
        try {
            return super.visitExpression(expression);
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Element visitVariableExpression(VariableExpression expr) {
        Var variable = Var.parse(expr.getVariable());
        if (context.isContextVar(variable)) {
            return context.resolveVar(variable);
        } else {
            try {
                var qualifier = context.getDefaultExpr();
                var qualifierType = (ClassType) qualifier.getType();
                var klass = qualifierType.resolve();
                var property = klass.getPropertyByVar(Var.parse(expr.getVariable()));
                return new PropertyExpression(qualifier, property.getRef());
            } catch (InternalException e) {
                throw new InternalException("Fail to resolve variable: " + expr.getVariable(), e);
            }
        }
    }

    @Override
    public Element visitArrayExpression(ArrayExpression array) {
        try {
            var assignedType = assignedTypeStack.peek();
            var assignedElementType = assignedType instanceof ArrayType arrayType ? arrayType.getElementType() : null;
            assignedTypeStack.push(assignedElementType);
            var elements = NncUtils.map(array.getExpressions(), expr -> (Expression) copy(expr));
            var types = NncUtils.map(elements, Expression::getType);
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
                    new ArrayType(elementType, ArrayKind.READ_WRITE)
            );
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Element visitConditionalExpression(ConditionalExpression expression) {
        assignedTypeStack.push(null);
        try {
            return ConditionalExpression.create(
                    (Expression) expression.getCondition().accept(this),
                    (Expression) expression.getTrueValue().accept(this),
                    (Expression) expression.getFalseValue().accept(this)
            );
        } finally {
            assignedTypeStack.pop();
        }
    }

    @Override
    public Element visitVariablePathExpression(VariablePathExpression expression) {
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
            var qualifier = (Expression) copy(expression.getQualifier());
            var qualifierType = (ClassType) context.getExpressionType(qualifier);
            var klass = qualifierType.resolve();
            var property = klass.getPropertyByVar(Var.parse(expression.getField().getVariable()));
            if (property == null)
                throw new InternalException("Property not found: " + expression.getField().getVariable() + " in type " + qualifierType);
            return new PropertyExpression(qualifier, property.getRef());
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

    @Override
    public Element visitAllMatchExpression(AllMatchExpression expression) {
        try {
            assignedTypeStack.push(null);
            var resolvedArray = (Expression) copy(expression.getArray());
            var alias = resolvedArray instanceof AsExpression asExpression ? asExpression.getAlias() : null;
            var elementType = ((ArrayType) resolvedArray.getType()).getElementType();
            var subContext = new SubParsingContext(alias, elementType, context);
            var condition = ExpressionResolverV2.resolve(expression.getCondition(), Types.getBooleanType(), subContext);
            return new AllMatchExpression(resolvedArray, condition);
        } finally {
            assignedTypeStack.pop();
        }
    }

}
