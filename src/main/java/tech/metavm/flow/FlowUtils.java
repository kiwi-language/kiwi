package tech.metavm.flow;

import tech.metavm.dto.ErrorCode;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;

public class FlowUtils {

    public static Type getExpressionType(Expression expression, @Nullable NodeRT<?> prev, ScopeRT scope) {
        var exprTypeMap = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
        return exprTypeMap.getType(expression);
    }

}
