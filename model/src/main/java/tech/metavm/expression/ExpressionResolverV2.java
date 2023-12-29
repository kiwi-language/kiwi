package tech.metavm.expression;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.CopyVisitor;
import tech.metavm.entity.Element;
import tech.metavm.entity.EntityProvider;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.*;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class ExpressionResolverV2 extends CopyVisitor {

    private final LinkedList<Type> assignedTypeStack = new LinkedList<>();

    public static Expression resolve(Expression expression, @Nullable Type assignedType, ParsingContext parsingContext) {
        return (Expression) expression.accept(new ExpressionResolverV2(expression, parsingContext, assignedType));
    }

    private final ParsingContext context;

    private ExpressionResolverV2(Expression root, ParsingContext context, @Nullable Type assignedType) {
        super(root);
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
            var qualifier = context.getDefaultExpr();
            var qualifierType = (ClassType) qualifier.getType();
            var property = qualifierType.getPropertyByVar(Var.parse(expr.getVariable()));
            return new PropertyExpression(qualifier, property);
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
                    elementType = StandardTypes.getNothingType();
                } else {
                    elementType = Types.getLeastUpperBound(types);
                }
            }
            return new ArrayExpression(
                    elements,
                    context.getArrayTypeProvider().getArrayType(elementType, ArrayKind.READ_WRITE)
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
                if (!context.isContextVar(qualifierVar) && context.getTypeProvider() != null) {
                    ClassType type = getClassType(qualifierVar, context.getTypeProvider());
                    if (type != null) {
                        return new StaticFieldExpression(
                                type.getStaticFieldByVar(Var.parse(expression.getField().getVariable()))
                        );
                    }
                }
            }
            var qualifier = (Expression) copy(expression.getQualifier());
            var qualifierType = (ClassType) context.getExpressionType(qualifier);
            var property = qualifierType.getPropertyByVar(Var.parse(expression.getField().getVariable()));
            return new PropertyExpression(qualifier, property);
        } finally {
            assignedTypeStack.pop();
        }
    }

    private ClassType getClassType(Var var, IndexedTypeProvider typeProvider) {
        return switch (var.getType()) {
            case ID -> {
                var entity = typeProvider.getClassType(var.getId());
                yield entity instanceof ClassType classType ? classType : null;
            }
            case NAME -> {
                var type = typeProvider.findClassTypeByName(var.getName());
                yield type instanceof ClassType classType ? classType : null;
            }
        };
    }

    @Override
    public Element visitAllMatchExpression(AllMatchExpression expression) {
        try {
            assignedTypeStack.push(null);
            Expression resolvedArray = (Expression) copy(expression.getArray());
            var allMatchExpr = new AllMatchExpression(resolvedArray, new ExpressionPlaceholder());
            SubParsingContext subContext = new SubParsingContext(allMatchExpr, context);
            allMatchExpr.setCondition(
                    ExpressionResolverV2.resolve(
                            expression.getCondition(), StandardTypes.getBooleanType(), subContext
                    )
            );
            return allMatchExpr;
        } finally {
            assignedTypeStack.pop();
        }
    }

}
