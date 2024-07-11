package org.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
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

    public static @NotNull FlowExecResult invoke(Method method, @Nullable ClassInstance self, List<? extends Instance> arguments, CallContext callContext) {
        if (method.isStatic()) {
            var nativeClass = tryGetNativeClass(method.getDeclaringType());
            NncUtils.requireNonNull(nativeClass,
                    "Native class not available for type '" + method.getDeclaringType().getName() + "'");
            List<Class<?>> paramTypes = new ArrayList<>();
            paramTypes.add(Klass.class);
            paramTypes.addAll(NncUtils.multipleOf(Instance.class, method.getParameters().size()));
            paramTypes.add(CallContext.class);
            Object[] args = new Object[2 + arguments.size()];
            args[0] = method.getDeclaringType();
            for (int i = 0; i < arguments.size(); i++) {
                args[i + 1] = arguments.get(i);
            }
            args[arguments.size() + 1] = callContext;
            var javaMethod = ReflectionUtils.getMethod(nativeClass, method.getCode(), paramTypes);
            var result = (Instance) ReflectionUtils.invoke(null, javaMethod, args);
            if (method.getReturnType().isVoid()) {
                return new FlowExecResult(null, null);
            } else {
                return new FlowExecResult(result, null);
            }
        } else {
            if (self instanceof ClassInstance classInstance) {
                Object nativeObject = getNativeObject(classInstance);
                var instanceClass = nativeObject.getClass();
                List<Class<?>> paramTypes = new ArrayList<>(NncUtils.multipleOf(Instance.class, method.getParameters().size()));
                paramTypes.add(CallContext.class);
                var args = new Object[arguments.size() + 1];
                for (int i = 0; i < arguments.size(); i++) {
                    args[i] = arguments.get(i);
                }
                args[arguments.size()] = callContext;
                var javaMethod = ReflectionUtils.getMethod(instanceClass, method.getCode(), paramTypes);
                var result = (Instance) ReflectionUtils.invoke(nativeObject, javaMethod, args);
                if (method.getReturnType().isVoid()) {
                    return new FlowExecResult(null, null);
                } else {
                    return new FlowExecResult(result, null);
                }
            } else
                throw new InternalException("Native invocation is not supported for non class instances");
        }
    }

    public static Object getNativeObject(ClassInstance instance) {
        var nativeObject = instance.getNativeObject();
        if (nativeObject == null) {
            nativeObject = createNativeObject(instance);
            instance.setNativeObject(nativeObject);
        }
        return nativeObject;
    }

    private static Object createNativeObject(ClassInstance instance) {
        var nativeClass = tryGetNativeClass(instance.getKlass());
        NncUtils.requireNonNull(nativeClass,
                "Native class not available for type '" + instance.getType().getName() + "'");
        Constructor<?> constructor = ReflectionUtils.getConstructor(nativeClass, ClassInstance.class);
        return ReflectionUtils.invokeConstructor(constructor, instance);
    }

    private static Class<?> tryGetNativeClass(Klass klass) {
        while (klass != null) {
//            var def = ModelDefRegistry.tryGetDef(klass.getEffectiveTemplate());
//            if (def != null) {
//                if (def instanceof DirectDef<?> directDef && directDef.getNativeClass() != null)
//                    return directDef.getNativeClass();
//                else
//                    return null;
            var nativeClass = klass.getEffectiveTemplate().getNativeClass();
            if(nativeClass != null)
                return nativeClass;
            else
                klass = NncUtils.get(klass.getSuperType(), ClassType::resolve);
        }
        return null;
    }

}
