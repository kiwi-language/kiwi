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
            var field = qualifierType.getFieldByVar(Var.parse(expr.getVariable()));
            return new FieldExpression(qualifier, field);
        }
//        String v = expr.getVariable();
//        List<String> fieldPath = Arrays.asList(v.split("\\."));
//        NncUtils.requireMinimumSize(fieldPath, 1);
//        Var firstVar = Var.parse(fieldPath.get(0));
//        if (context.isContextVar(firstVar)) {
//            if (fieldPath.size() == 1) {
//                return context.resolveVar(firstVar);
//            } else {
//                return createFieldExpression(
//                        context.resolveVar(firstVar),
//                        fieldPath.subList(1, fieldPath.size())
//                );
//            }
//        }
//        if (context.getInstanceContext() != null) {
//            ClassType type = getClassType(firstVar, context.getInstanceContext());
//            if (type != null) {
//                var field = type.getStaticFieldByName(fieldPath.get(1));
//                Expression curExpr = new StaticFieldExpression(field);
//                for (int i = 2; i < fieldPath.size(); i++) {
//                    ClassType curType = (ClassType) curExpr.getType();
//                    field = curType.getFieldByName(fieldPath.get(i));
//                    curExpr = new FieldExpression(curExpr, field);
//                }
//                return curExpr;
//            }
//        }
//        return createFieldExpression(context.getDefaultExpr(), fieldPath);
    }

    private Expression resolveVariablePath(VariablePathExpression expression) {
        if (expression.getQualifier() instanceof VariableExpression qualifierVariableExpr) {
            Var qualifierVar = Var.parse(qualifierVariableExpr.getVariable());
            if (context.getInstanceContext() != null) {
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
        var field = qualifierType.getFieldByVar(Var.parse(expression.getField().getVariable()));
        return new FieldExpression(qualifier, field);
    }

    private ClassType getClassType(Var var, IInstanceContext instanceContext) {
        return switch (var.getType()) {
            case ID -> {
                var entity = instanceContext.getEntityContext().getEntity(Object.class, var.getId());
                yield entity instanceof ClassType classType ? classType : null;
            }
            case NAME -> {
                var type = instanceContext.getEntityContext().selectByUniqueKey(Type.UNIQUE_NAME, var.getName());
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
