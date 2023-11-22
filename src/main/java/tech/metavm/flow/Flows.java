package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.Expression;
import tech.metavm.object.type.Type;

import javax.annotation.Nullable;

public class Flows {

    public static Type getExpressionType(Expression expression, @Nullable NodeRT<?> prev, ScopeRT scope) {
        var exprTypeMap = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
        return exprTypeMap.getType(expression);
    }

    public static void enableCache(Flow flow, IEntityContext entityContext) {
        entityContext.setLoadWithCache(flow);
        flow.accept(new WithCacheVisitor(entityContext));
    }

}
