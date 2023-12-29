package tech.metavm.entity.natives;

import tech.metavm.entity.DirectDef;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.List;

public class NativeMethods {

    public static FlowExecResult invoke(Method method, @Nullable Instance self, List<Instance> arguments) {
        if (self instanceof ClassInstance classInstance) {
            Object nativeObject = getNativeObject(classInstance);
            var instanceClass = nativeObject.getClass();
            List<Class<?>> paramTypes = NncUtils.multipleOf(Instance.class, method.getParameters().size());
            Instance[] args = new Instance[arguments.size()];
            arguments.toArray(args);
            var javaMethod = ReflectionUtils.getMethod(instanceClass, method.getCode(), paramTypes);
            var result = (Instance) ReflectionUtils.invoke(nativeObject, javaMethod, (Object[]) args);
            if (method.getReturnType().isVoid()) {
                return new FlowExecResult(null, null);
            } else {
                return new FlowExecResult(result, null);
            }
        } else
            throw new InternalException("Native invocation is not supported for non class instances");
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
        var nativeClass = tryGetNativeClass(instance.getType());
        NncUtils.requireNonNull(nativeClass,
                "Native class not available for type '" + instance.getType().getName() + "'");
        Constructor<?> constructor = ReflectionUtils.getConstructor(nativeClass, ClassInstance.class);
        return ReflectionUtils.invokeConstructor(constructor, instance);
    }

    private static Class<?> tryGetNativeClass(ClassType type) {
        var def = ModelDefRegistry.getDef(type.getEffectiveTemplate());
        if (def instanceof DirectDef<?> directDef && directDef.getNativeClass() != null)
            return directDef.getNativeClass();
        else
            return null;
    }

}
