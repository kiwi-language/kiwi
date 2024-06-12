package tech.metavm.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.expression.Expression;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Flows {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static Type getExpressionType(Expression expression, @Nullable NodeRT prev, ScopeRT scope) {
        var exprTypeMap = prev != null ? prev.getExpressionTypes() : scope.getExpressionTypes();
        return exprTypeMap.getType(expression);
    }

    public static boolean isConstructor(Flow flow) {
        return flow instanceof Method method && method.isConstructor();
    }

    public static @Nullable Klass getDeclaringType(Flow flow) {
        return flow instanceof Method method ? method.getDeclaringType() : null;
    }


    public static boolean isInstanceMethod(Flow flow) {
        return flow instanceof Method method && method.isInstanceMethod();
    }

    public static FunctionType getStaticType(Flow flow) {
        if (flow instanceof Method method && method.isInstanceMethod())
            return method.getStaticType();
        else
            throw new InternalException("Can not get static type of flow: " + flow);
    }

    public static FlowExecResult execute(Flow flow, @Nullable ClassInstance self, List<Instance> arguments, IEntityContext context) {
        try {
            ContextUtil.setEntityContext(context);
            return flow.execute(self, arguments, context.getInstanceContext());
        }
        finally {
            ContextUtil.setEntityContext(null);
        }
    }

    public static Instance invokeGetter(Method getter, ClassInstance instance, IEntityContext context) {
        var result = execute(getter, instance, List.of(), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return Objects.requireNonNull(result.ret());
    }

    public static void invokeSetter(Method setter, ClassInstance instance, Instance value, IEntityContext context) {
        var result = execute(setter, instance, List.of(value), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
    }

}
