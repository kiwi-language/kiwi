package org.metavm.entity.natives;

import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class JavaNativeInvoker {

    public static FlowExecResult invoke(Method method, @Nullable Value self, List<? extends Value> arguments, CallContext callContext) {
        var javaMethod = getJavaMethod(method);
        var paramTypes = Arrays.stream(javaMethod.getGenericParameterTypes()).iterator();
        var javaArgs = Utils.map(arguments, arg -> toJavaValue(arg, paramTypes.next(), callContext));
        try {
            var javaArgArray = javaArgs.toArray(Object[]::new);
            var ret = javaMethod.invoke(Objects.requireNonNull(self).resolveObject(), javaArgArray);
            return FlowExecResult.of(fromJavaValue(ret, method.getReturnType(), callContext));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static java.lang.reflect.Method getJavaMethod(Method method) {
        return method.getJavaMethod();
    }

    private static Value fromJavaValue(Object value, Type type, CallContext callContext) {
        if (value == null)
            return Instances.nullInstance();
        if (value instanceof String s)
            return Instances.stringInstance(s);
        throw new IllegalArgumentException("Cannot convert from java value: " + value);
    }

    private static Object toJavaValue(Value value, java.lang.reflect.Type javaType, CallContext callContext) {
        throw new UnsupportedOperationException();
    }

}
