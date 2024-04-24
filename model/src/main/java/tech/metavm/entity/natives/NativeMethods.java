package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.DirectDef;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Klass;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class NativeMethods {

    public static @NotNull FlowExecResult invoke(Method method, @Nullable Instance self, List<Instance> arguments, CallContext callContext) {
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

    private static Class<?> tryGetNativeClass(Klass type) {
        while (type != null) {
            var def = ModelDefRegistry.tryGetDef(type.getEffectiveTemplate().getType());
            if (def != null) {
                if (def instanceof DirectDef<?> directDef && directDef.getNativeClass() != null)
                    return directDef.getNativeClass();
                else
                    return null;
            } else
                type = type.getSuperClass();
        }
        return null;
    }

}
