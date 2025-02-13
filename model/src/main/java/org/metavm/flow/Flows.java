package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.BusinessException;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Flows {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static Type getExpressionType(Expression expression, @Nullable Node prev) {
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

    public static FunctionType getStaticType(FlowRef flow) {
        if (flow instanceof MethodRef method && method.isInstanceMethod())
            return method.getStaticType();
        else
            throw new InternalException("Can not get static type of flow: " + flow);
    }

    public static FlowExecResult execute(@NotNull FlowRef flow, @Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        return flow.execute(Utils.safeCall(self, Instance::getReference), arguments, callContext);
    }

    public static @Nullable Value invoke(@NotNull FlowRef flow, ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
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

    public static @Nullable Value invokeVirtual(@NotNull FlowRef flow, @NotNull ClassInstance self, List<? extends Value> arguments, IInstanceContext context) {
        if(flow instanceof MethodRef method && method.isInstanceMethod()) {
            flow = self.getInstanceType().getOverride(method);
            return invoke(flow, self, arguments, context);
        }
        else
            throw new InternalException("Can not invoke virtual method: " + flow);
    }

    public static @Nullable Value invokeVirtual(@NotNull FlowRef flow, @NotNull ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        if(flow instanceof MethodRef method && method.isInstanceMethod()) {
            flow = self.getInstanceType().getOverride(method);
            return invoke(flow, self, arguments, callContext);
        }
        else
            throw new InternalException("Can not invoke virtual method: " + flow);
    }

    public static Value invokeGetter(MethodRef getter, ClassInstance instance, IInstanceContext context) {
        var result = execute(getter, instance, List.of(), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
        else
            return Objects.requireNonNull(result.ret());
    }

    public static void invokeSetter(MethodRef setter, ClassInstance instance, Value value, IInstanceContext context) {
        var result = execute(setter, instance, List.of(value), context);
        if(result.exception() != null)
            throw new BusinessException(ErrorCode.FLOW_EXECUTION_FAILURE, ThrowableNative.getMessage(result.exception()));
    }

    public static boolean isValuesMethod(Method method) {
        return method.isStatic() && method.getName().equals("values") && method.getParameters().isEmpty();
    }

    public static Method saveValuesMethod(Klass klass) {
        assert klass.isEnum();
        var m = klass.findSelfMethod(Flows::isValuesMethod);
        if(m == null) {
            m = MethodBuilder.newBuilder(klass, "values")
                    .id(TmpId.random())
                    .isStatic(true)
                    .returnType(new ArrayType(klass.getType(), ArrayKind.DEFAULT))
                    .build();
        }
        return m;
    }

    public static void generateValuesMethodBody(Klass klass) {
        assert klass.isEnum();
        var valuesMethod = klass.getSelfMethod(Flows::isValuesMethod);
        var retType = valuesMethod.getReturnType();
        valuesMethod.clearContent();
        valuesMethod.setReturnType(retType); // Add return type to constant pool
        var code = valuesMethod.getCode();
        var arrayType = new ArrayType(klass.getType(), ArrayKind.DEFAULT);
        Nodes.newArray(arrayType, code);
        var arrayVar = code.nextVariableIndex();
        Nodes.store(arrayVar, code);
        for (var ec : klass.getEnumConstants()) {
            Nodes.load(arrayVar, arrayType, code);
            Nodes.getStaticField(ec, code);
            Nodes.addElement(code);
        }
        Nodes.load(arrayVar, arrayType, code);
        Nodes.ret(code);
    }

}
