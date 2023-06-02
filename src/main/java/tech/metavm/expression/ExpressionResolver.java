package tech.metavm.expression;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
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
        if(expression instanceof VariableExpression variableExpression) {
            return resolveVariable(variableExpression);
        }
        else if(expression instanceof AllMatchExpression allMatchExpression) {
            return resolveAllMatch(allMatchExpression);
        }
        else {
            List<Expression> children = expression.getChildren();
            List<Expression> resolvedChildren = NncUtils.map(children, this::resolve);
            return expression.cloneWithNewChildren(resolvedChildren);
        }
    }

    private Expression resolveVariable(VariableExpression expr) {
        String v = expr.getVariable();
        List<String> fieldPath = Arrays.asList(v.split("\\."));
        NncUtils.requireMinimumSize(fieldPath, 1);
        Var firstVar = Var.parse(fieldPath.get(0));
        if(context.isContextVar(firstVar)) {
            if(fieldPath.size() == 1) {
                return context.resolveVar(firstVar);
            }
            else {
                return createFieldExpression(
                        context.resolveVar(firstVar),
                        fieldPath.subList(1, fieldPath.size())
                );
            }
        }
        else {
            return createFieldExpression(context.getDefaultExpr(), fieldPath);
        }
    }

    private AllMatchExpression resolveAllMatch(AllMatchExpression expression) {
        Expression resolvedArray = resolve(expression.getArray());
        String alias = ExpressionUtil.getAlias(resolvedArray);
        CursorExpression cursor = new CursorExpression(resolvedArray, alias);
        SubParsingContext subContext = new SubParsingContext(cursor, context);
        Expression resolvedCondition = ExpressionResolver.resolve(expression.getCondition(), subContext);
        return new AllMatchExpression(resolvedArray, resolvedCondition, cursor);
    }

    private FieldExpression createFieldExpression(Expression instance, List<String> fieldPath) {
        Type type = instance.getType();
        if(type instanceof ClassType classType) {
            List<Field> fields = classType.getFieldsByPath(fieldPath);
            return new FieldExpression(instance, fields);
        }
        else {
            throw new InternalException("Instance type must be ClassType");
        }
    }

}
