package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.FunctionValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Flows {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static Type getExpressionType(Expression expression, @Nullable NodeRT prev) {
        var exprTypeMap = prev != null ? prev.getNextExpressionTypes() : ExpressionTypeMap.EMPTY;
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

    public static FlowExecResult execute(Flow flow, @Nullable ClassInstance self, List<? extends Value> arguments, IEntityContext context) {
        return execute(flow, self, arguments, context.getInstanceContext());
    }

    public static FlowExecResult execute(@NotNull Flow flow, @Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        return flow.execute(self, arguments, callContext);
    }

    public static @Nullable Value invoke(@NotNull Flow flow, ClassInstance self, List<? extends Value> arguments, IEntityContext context) {
        return invoke(flow, self, arguments, context.getInstanceContext());
    }

    public static @Nullable Value invoke(@NotNull Flow flow, ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        var result = execute(flow, self, arguments, callContext);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return result.ret();
    }

    public static @Nullable Value invoke(@NotNull FunctionValue func, List<? extends Value> arguments, CallContext callContext) {
        var result = func.execute(arguments, callContext);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return result.ret();
    }

    public static @Nullable Value invokeVirtual(@NotNull Flow flow, @NotNull ClassInstance self, List<? extends Value> arguments, IEntityContext context) {
        if(flow instanceof Method method && method.isInstanceMethod()) {
            flow = self.getKlass().resolveMethod(method);
            return invoke(flow, self, arguments, context);
        }
        else
            throw new InternalException("Can not invoke virtual method: " + flow);
    }

    public static @Nullable Value invokeVirtual(@NotNull Flow flow, @NotNull ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        if(flow instanceof Method method && method.isInstanceMethod()) {
            flow = self.getKlass().resolveMethod(method);
            return invoke(flow, self, arguments, callContext);
        }
        else
            throw new InternalException("Can not invoke virtual method: " + flow);
    }

    public static Value invokeGetter(Method getter, ClassInstance instance, IEntityContext context) {
        var result = execute(getter, instance, List.of(), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return Objects.requireNonNull(result.ret());
    }

    public static void invokeSetter(Method setter, ClassInstance instance, Value value, IEntityContext context) {
        var result = execute(setter, instance, List.of(value), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
    }

    public static boolean isValuesMethod(Method method) {
        return method.isStatic() && method.getName().equals("values") && method.getParameters().isEmpty();
    }

    public static Method saveValuesMethod(Klass klass) {
        assert klass.isEnum();
        var m = klass.findMethod(Flows::isValuesMethod);
        if(m == null) {
            m = MethodBuilder.newBuilder(klass, "values", "values")
                    .isStatic(true)
                    .returnType(new ArrayType(klass.getType(), ArrayKind.READ_WRITE))
                    .build();
        }
        return m;
    }

    public static void generateValuesMethodBody(Klass klass) {
        assert klass.isEnum();
        var valuesMethod = klass.getMethod(Flows::isValuesMethod);
        valuesMethod.clearContent();
        var scope = valuesMethod.getScope();
        var arrayType = new ArrayType(klass.getType(), ArrayKind.READ_WRITE);
        Nodes.newArray(arrayType, scope);
        var arrayVar = scope.nextVariableIndex();
        Nodes.store(arrayVar, scope);
        for (var ecd : klass.getEnumConstantDefs()) {
            Nodes.load(arrayVar, arrayType, scope);
            Nodes.getStatic(ecd.getField(), scope);
            Nodes.addElement(scope);
        }
        Nodes.load(arrayVar, arrayType, scope);
        Nodes.ret(scope);
    }

}
