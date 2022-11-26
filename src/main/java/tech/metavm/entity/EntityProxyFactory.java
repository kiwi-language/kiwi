package tech.metavm.entity;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EntityProxyFactory {

    private EntityProxyFactory() {}

    private static final Map<Class<?>, Class<?>> PROXY_CLASS_MAP = new ConcurrentHashMap<>();
    private static final Field FIELD_PERSISTED = ReflectUtils.getField(Entity.class, "persisted");
//    private static final Field FIELD_CONTEXT = ReflectUtils.getField(Entity.class, "context");
    private static final Field FIELD_ID = ReflectUtils.getField(Entity.class, "id");

    public static <T> T getProxyInstance(Class<T> type, IInstance instance, Function<Instance, Object> modelCreator) {
        Class<?> proxyClass = getProxyClass(type);
        try {
            ProxyObject proxyInstance = (ProxyObject) ReflectUtils.getUnsafe().allocateInstance(proxyClass);
            proxyInstance.setHandler(new EntityMethodHandler(instance, modelCreator));
            FIELD_ID.set(proxyInstance, instance.getId());
            FIELD_PERSISTED.set(proxyInstance, true);
            return type.cast(proxyInstance);
        }
        catch (Exception e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    public static <T extends Entity> T makeDummy(Class<T> type, long id) {
        Class<?> proxyClass = getProxyClass(type);
        try {
            ProxyObject proxyInstance = (ProxyObject) ReflectUtils.getUnsafe().allocateInstance(proxyClass);
            FIELD_ID.set(proxyInstance, id);
            FIELD_PERSISTED.set(proxyInstance, true);
            return type.cast(proxyInstance);
        }
        catch (Exception e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    private static Class<?> getProxyClass(Class<?> type) {
        return PROXY_CLASS_MAP.computeIfAbsent(type, t -> {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setSuperclass(type);
            return proxyFactory.createClass();
        });
    }

}
