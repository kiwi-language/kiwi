package tech.metavm.entity;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class EntityProxyFactory {

    private EntityProxyFactory() {}

    private static final Map<Class<?>, Class<?>> PROXY_CLASS_MAP = new ConcurrentHashMap<>();
    private static final Field FIELD_PERSISTED = ReflectUtils.getField(Entity.class, "persisted");
    private static final Field FIELD_ID = ReflectUtils.getField(Entity.class, "id");

    public static <T> T getProxyInstance(TypeReference<T> typeRef, Supplier<T> modelSupplier) {
        return getProxyInstance(typeRef.getType(), null, modelSupplier);
    }

    public static <T> T getProxyInstance(Class<? extends T> type, Supplier<T> modelSupplier) {
        return getProxyInstance(type, null, modelSupplier);
    }

    public static <T> T getProxyInstance(Class<? extends T> type, Long id, Supplier<T> modelSupplier) {
        Class<?> proxyClass = getProxyClass(type);
        try {
            ProxyObject proxyInstance = (ProxyObject) ReflectUtils.getUnsafe().allocateInstance(proxyClass);
            proxyInstance.setHandler(new EntityMethodHandler(modelSupplier));
            if(id != null && (proxyInstance instanceof IdInitializing idInitializing)) {
                idInitializing.initId(id);
            }
            return type.cast(proxyInstance);
        }
        catch (Exception e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T extractReal(T object) {
        if(object instanceof ProxyObject proxyObject) {
            EntityMethodHandler handler = (EntityMethodHandler) proxyObject.getHandler();
            return (T) handler.getEntity();
        }
        else {
            return object;
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
            proxyFactory.setFilter(EntityProxyFactory::shouldIntercept);
            return proxyFactory.createClass();
        });
    }

    private static boolean shouldIntercept(Method method) {
        return !isGetIdMethod(method) && !isInitId(method);
    }

    private static boolean isGetIdMethod(Method method) {
        return method.getName().equals("getId") &&
                (method.getReturnType() == Long.class || method.getReturnType() == long.class)
                && method.getParameterCount() == 0;
    }

    private static boolean isInitId(Method method) {
        return method.getName().equals("initId") &&
                method.getReturnType() == void.class
                && method.getParameterCount() == 1 && method.getParameterTypes()[0] == long.class;
    }

}
