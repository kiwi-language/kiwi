package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class NativeMethods {

    public static final Logger logger = LoggerFactory.getLogger(NativeMethods.class);

    public static @NotNull FlowExecResult invoke(Method method, @Nullable ClassInstance self, List<? extends Value> arguments, CallContext callContext) {
        if (method.isStatic()) {
            Object[] args = new Object[2 + arguments.size()];
            args[0] = method.getDeclaringType();
            for (int i = 0; i < arguments.size(); i++) {
                args[i + 1] = arguments.get(i);
            }
            args[arguments.size() + 1] = callContext;
            var javaMethod = getNativeMethod(method, null);
            var result = (Value) ReflectionUtils.invoke(null, javaMethod, args);
            if (method.getReturnType().isVoid()) {
                return new FlowExecResult(null, null);
            } else {
                return new FlowExecResult(result, null);
            }
        } else {
            if (self instanceof ClassInstance classInstance) {
                Object nativeObject = getNativeObject(classInstance);
                var args = new Object[arguments.size() + 1];
                for (int i = 0; i < arguments.size(); i++) {
                    args[i] = arguments.get(i);
                }
                args[arguments.size()] = callContext;
                var javaMethod = getNativeMethod(method, nativeObject);
                var result = (Value) ReflectionUtils.invoke(nativeObject, javaMethod, args);
                if (method.getReturnType().isVoid()) {
                    return new FlowExecResult(null, null);
                } else {
                    return new FlowExecResult(result, null);
                }
            } else
                throw new InternalException("Native invocation is not supported for non class instances");
        }
    }

    private static java.lang.reflect.Method getNativeMethod(Method method, @Nullable Object nativeObject) {
        var nativeMethod = method.getNativeMethod();
        if (nativeMethod == null) {
            var nativeClass = nativeObject != null ? nativeObject.getClass() : tryGetNativeClass(method.getDeclaringType());
            NncUtils.requireNonNull(nativeClass,
                    "Native class not available for type '" + method.getDeclaringType().getName() + "'");
            List<Class<?>> paramTypes = new ArrayList<>();
            if (method.isStatic())
                paramTypes.add(Klass.class);
            paramTypes.addAll(NncUtils.multipleOf(Value.class, method.getParameters().size()));
            paramTypes.add(CallContext.class);
            try {
                nativeMethod = ReflectionUtils.getMethod(nativeClass, method.getNativeName(), paramTypes);
            } catch (Exception e) {
                logger.warn("Cannot find native method for method " + method + " using native name '" + method.getNativeName() + "'");
                nativeMethod = ReflectionUtils.getMethod(nativeClass, method.getName(), paramTypes);
            }
            method.setNativeMethod(nativeMethod);
        }
        return nativeMethod;
    }

    public static Object getNativeObject(ClassInstance instance) {
        var nativeObject = instance.getNativeObject();
        if (nativeObject == null) {
            nativeObject = createNativeObject(instance);
            instance.setNativeObject(nativeObject);
        }
        return nativeObject;
    }

    private static NativeBase createNativeObject(ClassInstance instance) {
        var nativeClass = tryGetNativeClass(instance.getKlass());
        NncUtils.requireNonNull(nativeClass,
                "Native class not available for type '" + instance.getType().getTypeDesc() + "'");
        Constructor<?> constructor = ReflectionUtils.getConstructor(nativeClass, ClassInstance.class);
        return (NativeBase) ReflectionUtils.invokeConstructor(constructor, instance);
    }

    private static Class<?> tryGetNativeClass(Klass klass) {
        while (klass != null) {
            var nativeClass = klass.getNativeClass();
            if(nativeClass != null)
                return nativeClass;
            else
                klass = NncUtils.get(klass.getSuperType(), ClassType::getKlass);
        }
        return null;
    }

}
