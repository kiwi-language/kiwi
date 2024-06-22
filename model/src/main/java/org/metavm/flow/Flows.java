package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static FlowExecResult execute(Flow flow, @Nullable ClassInstance self, List<? extends Instance> arguments, IEntityContext context) {
        ContextUtil.setEntityContext(context);
        return execute(flow, self, arguments, context.getInstanceContext());
    }

    public static FlowExecResult execute(Flow flow, @Nullable ClassInstance self, List<? extends Instance> arguments, CallContext callContext) {
        try {
            return flow.execute(self, arguments, callContext);
        }
        finally {
            ContextUtil.setEntityContext(null);
        }
    }

    public static @Nullable Instance invoke(Flow flow, ClassInstance self, List<? extends Instance> arguments, IEntityContext context) {
        ContextUtil.setEntityContext(context);
        return invoke(flow, self, arguments, context.getInstanceContext());
    }

    public static @Nullable Instance invoke(Flow flow, ClassInstance self, List<? extends Instance> arguments, CallContext callContext) {
        var result = execute(flow, self, arguments, callContext);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return result.ret();
    }

    public static @Nullable Instance invokeVirtual(Flow flow, @NotNull ClassInstance self, List<? extends Instance> arguments, IEntityContext context) {
        if(flow instanceof Method method && method.isInstanceMethod()) {
            flow = self.getKlass().resolveMethod(method);
            return invoke(flow, self, arguments, context);
        }
        else
            throw new InternalException("Can not invoke virtual method: " + flow);
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
