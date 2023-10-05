package tech.metavm.entity.natives;

import tech.metavm.flow.Flow;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class NativeInvoker {

    public static Instance invoke(Flow flow, Instance instance, List<Instance> arguments) {
        Object nativeObject = getNativeObject(instance);
        var instanceClass = nativeObject.getClass();
        List<Class<?>> paramTypes = NncUtils.multipleOf(Instance.class, flow.getParameters().size());
        Instance[] args = new Instance[arguments.size()];
        arguments.toArray(args);
        var method = ReflectUtils.getMethod(instanceClass, flow.getCode(), paramTypes);
        var result =  (Instance) ReflectUtils.invoke(nativeObject, method, args);
        if(flow.getReturnType().isVoid()) {
            return null;
        }
        else {
            return result;
        }
    }

    public static Object getNativeObject(Instance instance) {
        var nativeObject = instance.getNativeObject();
        if(nativeObject == null) {
            nativeObject = createNativeObject(instance);
            instance.setNativeObject(nativeObject);
        }
        return nativeObject;
    }

    private static Object createNativeObject(Instance instance) {
        Class<?> nativeClass = NncUtils.requireNonNull(instance.getType().getNativeClass(),
                "类型" + instance.getType() + "不支持原生调用");
        Constructor<?> constructor = ReflectUtils.getConstructor(nativeClass,
                ClassInstance.class);
        return ReflectUtils.invokeConstructor(constructor, instance);
    }


}
