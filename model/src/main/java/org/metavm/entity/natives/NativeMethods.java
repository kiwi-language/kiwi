package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Entity;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NativeMethods {

    public static final Logger logger = LoggerFactory.getLogger(NativeMethods.class);

    public static @NotNull FlowExecResult invoke(Method method, @Nullable Value self, List<? extends Value> arguments, CallContext callContext) {
        try {
            if (method.isStatic()) {
                Object[] args = new Object[2 + arguments.size()];
                args[0] = method.getDeclaringType();
                for (int i = 0; i < arguments.size(); i++) {
                    args[i + 1] = arguments.get(i);
                }
                args[arguments.size() + 1] = callContext;
                var mh = getNativeMethod(method, null);
                var result = (Value) mh.invokeExact(args);
                if (method.getReturnType().isVoid()) {
                    return new FlowExecResult(null, null);
                } else {
                    return new FlowExecResult(result, null);
                }
            } else {
                if (self instanceof PrimitiveValue primitiveValue) {
                    var mh = Objects.requireNonNull(method.getNativeHandle(),
                            () -> "Cannot find native handle for method: " + method);
                    var args = new Value[arguments.size() + 1];
                    args[0] = primitiveValue;
                    for (int i = 1; i < args.length; i++) {
                        args[i] = arguments.get(i - 1);
                    }
                    //noinspection ConfusingArgumentToVarargsMethod
                    var result = (Value) mh.invokeExact(args);
                    if (method.getReturnType().isVoid()) {
                        return new FlowExecResult(null, null);
                    } else {
                        return new FlowExecResult(result, null);
                    }
                } else if (self instanceof Reference ref && ref.get() instanceof ClassInstance classInstance) {
                    Object nativeObject = getNativeObject(classInstance);
                    var args = new Object[arguments.size() + 2];
                    args[0] = nativeObject;
                    for (int i = 0; i < arguments.size(); i++) {
                        args[i + 1] = arguments.get(i);
                    }
                    args[arguments.size() + 1] = callContext;
                    var mh = getNativeMethod(method, nativeObject);
                    var result = (Value) mh.invokeExact(args);
                    if (method.getReturnType().isVoid()) {
                        return new FlowExecResult(null, null);
                    } else {
                        return new FlowExecResult(result, null);
                    }
                } else
                    throw new InternalException("Native invocation is not supported for non class instance: " + self.resolveObject());
            }
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle getNativeMethod(Method method, @Nullable Object nativeObject) {
        var nativeMethod = method.getNativeHandle();
        if (nativeMethod == null) {
            var nativeClass = nativeObject != null ? nativeObject.getClass() : tryGetNativeClass(method.getDeclaringType());
            Objects.requireNonNull(nativeClass,
                    "Native class not available for type '" + method.getDeclaringType().getName() + "'");
            List<Class<?>> paramTypes = new ArrayList<>();
            if (method.isStatic())
                paramTypes.add(Klass.class);
            paramTypes.addAll(Utils.multipleOf(Value.class, method.getParameters().size()));
            paramTypes.add(CallContext.class);
            try {
                nativeMethod = ReflectionUtils.getMethodHandleWithSpread(MethodHandles.lookup(), nativeClass, method.getNativeName(),
                        Value.class, paramTypes, method.isStatic());
            } catch (Exception e) {
                logger.warn("Cannot find native method for method " + method + " using native name '" + method.getNativeName() + "'");
                nativeMethod = ReflectionUtils.getMethodHandleWithSpread(MethodHandles.lookup(), nativeClass, method.getName(),
                        Value.class, paramTypes, method.isStatic());
            }
            method.setNativeHandle(nativeMethod);
        }
        return nativeMethod;
    }

    public static Object getNativeObject(ClassInstance instance) {
        if (instance instanceof Entity || instance instanceof NativeEphemeralObject)
            return instance;
        else if (instance instanceof MvClassInstance mvInst) {
            var nativeObject = mvInst.getNativeObject();
            if (nativeObject == null) {
                nativeObject = createNativeObject(mvInst);
                mvInst.setNativeObject(nativeObject);
            }
            return nativeObject;
        }
        else
            throw new IllegalArgumentException("Unrecognized class instance: " + instance);
    }

    private static NativeBase createNativeObject(ClassInstance instance) {
        var nativeClass = tryGetNativeClass(instance.getInstanceKlass());
        Objects.requireNonNull(nativeClass,
                "Native class not available for type '" + instance.getInstanceType().getTypeDesc() + "'");
        Constructor<?> constructor = ReflectionUtils.getConstructor(nativeClass, ClassInstance.class);
        return (NativeBase) ReflectionUtils.invokeConstructor(constructor, instance);
    }

    private static Class<?> tryGetNativeClass(Klass klass) {
        while (klass != null) {
            var nativeClass = klass.getNativeClass();
            if(nativeClass != null)
                return nativeClass;
            else
                klass = Utils.safeCall(klass.getSuperType(), ClassType::getKlass);
        }
        return null;
    }

}
