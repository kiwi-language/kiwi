package tech.metavm.expression;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public class ExpressionResolver {

    public static Expression resolve(Expression expression, ParsingContext parsingContext) {
        return new ExpressionResolver(parsingContext).resolve(expression);
    }

    private final ParsingContext context;

    private ExpressionResolver(ParsingContext context) {
        this.context = context;
    }

    private Expression resolve(Expression expression) {
        switch (expression) {
            case VariableExpression variableExpression -> {
                return resolveVariable(variableExpression);
            }
            case VariablePathExpression variablePathExpression -> {
                return resolveVariablePath(variablePathExpression);
            }
            case AllMatchExpression allMatchExpression -> {
                return resolveAllMatch(allMatchExpression);
            }
            default -> {
                List<Expression> children = expression.getChildren();
                List<Expression> resolvedChildren = NncUtils.map(children, this::resolve);
                return expression.cloneWithNewChildren(resolvedChildren);
            }
        }
    }

    private Expression resolveVariable(VariableExpression expr) {
        Var variable = Var.parse(expr.getVariable());
        if (context.isContextVar(variable)) {
            return context.resolveVar(variable);
        } else {
            var qualifier = context.getDefaultExpr();
            var qualifierType = (ClassType) qualifier.getType();
            var attr = qualifierType.getAttributeByVar(Var.parse(expr.getVariable()));
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
        var qualifier = resolve(expression.getQualifier());
        var qualifierType = (ClassType) context.getExpressionType(qualifier);
        var attr = qualifierType.getAttributeByVar(Var.parse(expression.getField().getVariable()));
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
        Expression resolvedArray = resolve(expression.getArray());
        String alias = ExpressionUtil.getAlias(resolvedArray);
        CursorExpression cursor = new CursorExpression(resolvedArray, alias);
        SubParsingContext subContext = new SubParsingContext(cursor, context);
        Expression resolvedCondition = ExpressionResolver.resolve(expression.getCondition(), subContext);
        return new AllMatchExpression(resolvedArray, resolvedCondition, cursor);
    }

}
