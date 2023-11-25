package tech.metavm.entity.natives;

import tech.metavm.entity.DirectDef;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.util.List;

public class NativeInvoker {

    public static FlowExecResult invoke(Flow flow, Instance instance, List<Instance> arguments) {
        if (instance instanceof ClassInstance classInstance) {
            Object nativeObject = getNativeObject(classInstance);
            var instanceClass = nativeObject.getClass();
            List<Class<?>> paramTypes = NncUtils.multipleOf(Instance.class, flow.getParameters().size());
            Instance[] args = new Instance[arguments.size()];
            arguments.toArray(args);
            var method = ReflectUtils.getMethod(instanceClass, flow.getCode(), paramTypes);
            var result = (Instance) ReflectUtils.invoke(nativeObject, method, (Object[]) args);
            if (flow.getReturnType().isVoid()) {
                return new FlowExecResult(null, null);
            } else {
                return new FlowExecResult(result, null);
            }
        } else {
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
        var nativeClass = tryGetNativeClass(instance.getType());
        NncUtils.requireNonNull(nativeClass,
                "Native class not available for type '" + instance.getType().getName() + "'");
        Constructor<?> constructor = ReflectUtils.getConstructor(nativeClass, ClassInstance.class);
        return ReflectUtils.invokeConstructor(constructor, instance);
    }

    private static Class<?> tryGetNativeClass(ClassType type) {
        var def = ModelDefRegistry.getDef(type.getEffectiveTemplate());
        if (def instanceof DirectDef<?> directDef && directDef.getNativeClass() != null)
            return directDef.getNativeClass();
        else
            return null;
    }

}
