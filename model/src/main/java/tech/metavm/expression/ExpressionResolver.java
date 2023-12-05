package tech.metavm.expression;

import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public class ExpressionResolver {

    public static Expression resolve(Expression expression, @Nullable Type assignedType, ParsingContext parsingContext) {
        return new ExpressionResolver(parsingContext).resolve(expression, assignedType);
    }

    private final ParsingContext context;

    private ExpressionResolver(ParsingContext context) {
        this.context = context;
    }

    private Expression resolve(Expression expression, @Nullable Type assignedType) {
        return switch (expression) {
            case VariableExpression variableExpression -> resolveVariable(variableExpression);
            case VariablePathExpression variablePathExpression -> resolveVariablePath(variablePathExpression);
            case AllMatchExpression allMatchExpression -> resolveAllMatch(allMatchExpression);
            case ArrayExpression arrayExpression -> resolveArray(arrayExpression, assignedType);
            default -> expression.substituteChildren(
                    NncUtils.map(expression.getChildren(), child -> resolve(child, assignedType))
            );
        };
    }

    private Expression resolveArray(ArrayExpression array, @Nullable Type assignedType) {
        var assignedElementType = assignedType instanceof ArrayType arrayType ? arrayType.getElementType() : null;
        var elements = NncUtils.map(array.getExpressions(), expr -> resolve(expr, assignedElementType));
        var types = NncUtils.map(elements, Expression::getType);
        Type elementType = assignedElementType;
        if(elementType == null) {
            if(types.isEmpty()) {
                elementType = StandardTypes.getNothingType();
            }
            else {
                elementType = Types.getLeastUpperBound(types);
            }
        }
        return new ArrayExpression(
                elements,
                Objects.requireNonNull(context.getEntityContext())
                        .getArrayType(elementType, ArrayKind.READ_WRITE)
        );
    }

    private Expression resolveVariable(VariableExpression expr) {
        Var variable = Var.parse(expr.getVariable());
        if (context.isContextVar(variable)) {
            return context.resolveVar(variable);
        } else {
            var qualifier = context.getDefaultExpr();
            var qualifierType = (ClassType) qualifier.getType();
            var attr = qualifierType.getPropertyByVar(Var.parse(expr.getVariable()));
            return new PropertyExpression(qualifier, attr);
        }
    }

    private Expression resolveVariablePath(VariablePathExpression expression) {
        if (expression.getQualifier() instanceof VariableExpression qualifierVariableExpr) {
            Var qualifierVar = Var.parse(qualifierVariableExpr.getVariable());
            if (!context.isContextVar(qualifierVar) && context.getInstanceContext() != null) {
                ClassType type = getClassType(qualifierVar, context.getInstanceContext());
                if (type != null) {
                    return new StaticFieldExpression(
                            type.getStaticFieldByVar(Var.parse(expression.getField().getVariable()))
                    );
                }
            }
        }
        var qualifier = resolve(expression.getQualifier(), null);
        var qualifierType = (ClassType) context.getExpressionType(qualifier);
        var attr = qualifierType.getPropertyByVar(Var.parse(expression.getField().getVariable()));
        return new PropertyExpression(qualifier, attr);
    }

    private ClassType getClassType(Var var, IInstanceContext instanceContext) {
        return switch (var.getType()) {
            case ID -> {
                var entity = instanceContext.getEntityContext().getEntity(Object.class, var.getId());
                yield entity instanceof ClassType classType ? classType : null;
            }
            case NAME -> {
                var type = instanceContext.getEntityContext().selectByUniqueKey(ClassType.UNIQUE_NAME, var.getName());
                yield type instanceof ClassType classType ? classType : null;
            }
        };
    }

    private AllMatchExpression resolveAllMatch(AllMatchExpression expression) {
        Expression resolvedArray = resolve(expression.getArray(), null);
        String alias = ExpressionUtil.getAlias(resolvedArray);
        CursorExpression cursor = new CursorExpression(resolvedArray, alias);
        SubParsingContext subContext = new SubParsingContext(cursor, context);
        Expression resolvedCondition = ExpressionResolver.resolve(
                expression.getCondition(), StandardTypes.getBooleanType(), subContext
        );
        return new AllMatchExpression(resolvedArray, resolvedCondition, cursor);
    }

}
