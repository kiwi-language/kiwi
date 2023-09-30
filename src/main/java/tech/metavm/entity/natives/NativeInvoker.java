package tech.metavm.entity.natives;

import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;

public class NativeInvoker {

    public static Instance invoke(Flow flow, Instance instance, ClassInstance argument) {
        Object nativeObject = getNativeObject(instance);
        var instanceClass = nativeObject.getClass();
        var method = ReflectUtils.getMethod(
                instanceClass, flow.getCode(),
                NncUtils.map(
                        flow.getInputType().getFields(),
                        field -> Instance.class
                )
        );
        var arguments = NncUtils.map(flow.getInputType().getFields(), argument::get);
        Object[] args = new Object[arguments.size()];
        arguments.toArray(args);
        var result =  (Instance) ReflectUtils.invoke(nativeObject, method, args);
        if(flow.getOutputType().isVoid()) {
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
