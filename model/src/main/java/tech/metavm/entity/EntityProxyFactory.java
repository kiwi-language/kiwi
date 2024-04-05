package tech.metavm.entity;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class EntityProxyFactory {

    private EntityProxyFactory() {}

    private static final Map<Class<?>, Class<?>> PROXY_CLASS_MAP = new ConcurrentHashMap<>();
    private static final Field FIELD_PERSISTED = ReflectionUtils.getField(Entity.class, "persisted");
    private static final Field FIELD_ID = ReflectionUtils.getField(Entity.class, "id");
    private static final Method HASHCODE_METHOD = ReflectionUtils.getMethod(Object.class, "hashCode");

    public static <T> T getProxy(TypeReference<T> typeRef, Consumer<T> modelSupplier) {
        return getProxy(typeRef.getType(), null, modelSupplier);
    }

    public static <T> T getProxy(Class<T> type, Consumer<T> initializer) {
        return getProxy(type, null, initializer);
    }

    public static <T> T getProxy(Class<T> type,
                                 @Nullable Id id,
                                 Consumer<T> initializer) {
        return getProxy(type, id, ReflectionUtils::allocateInstance, initializer);
    }

    public static <T> T getProxy(TypeReference<T> type,
                                 @Nullable Id id,
                                 Consumer<T> initializer,
                                 Function<Class<? extends T>, T> constructor) {
        return getProxy(type.getType(), id, constructor, initializer);
    }

    public static <T> T getProxy(Class<T> type,
                                 @Nullable Id id,
                                 Function<Class<? extends T>, T> constructor,
                                 Consumer<T> initializer) {
        Class<? extends T> proxyClass = getProxyClass(type).asSubclass(type);
        try {
            ProxyObject proxyInstance =  (ProxyObject) constructor.apply(proxyClass);
            proxyInstance.setHandler(new EntityMethodHandler<>(type, initializer));
            if(id != null && (proxyInstance instanceof IdInitializing idInitializing)) {
                idInitializing.initId(id);
            }
            return type.cast(proxyInstance);
        }
        catch (Exception e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    public final static IdentitySet<Object> DUMMIES = new IdentitySet<>();

    public static Object makeDummy(Class<?> type, Object extra) {
        Class<?> proxyClass = getProxyClass(type);
        try {
            var dummy = (ProxyObject) ReflectionUtils.getUnsafe().allocateInstance(proxyClass);
            DUMMIES.add(dummy);
            dummy.setHandler(new DummyHandler(extra));
            return dummy;
        } catch (InstantiationException e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    public static boolean isDummy(Object object) {
        return object instanceof ProxyObject proxyObject && proxyObject.getHandler() instanceof DummyHandler;
    }

    public static Object getDummyExtra(Object object) {
        return ((DummyHandler) ((ProxyObject) object).getHandler()).extra();
    }

    private record DummyHandler(Object extra) implements MethodHandler {

        @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws InvocationTargetException, IllegalAccessException {
                return proceed.invoke(self, args);
            }
        }

    public static <T extends Entity> T makeEntityDummy(Class<T> type, Id id) {
        try {
            ProxyObject proxyInstance = (ProxyObject) makeDummy(type, id);
            FIELD_ID.set(proxyInstance, id);
            FIELD_PERSISTED.set(proxyInstance, true);
            return type.cast(proxyInstance);
        }
        catch (Exception e) {
            throw new RuntimeException("fail to create proxy instance", e);
        }
    }

    public static <T> Class<? extends T> getProxyClass(Class<T> type) {
        return PROXY_CLASS_MAP.computeIfAbsent(type, t -> {
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setSuperclass(type);
            proxyFactory.setFilter(EntityProxyFactory::shouldIntercept);
            return proxyFactory.createClass();
        }).asSubclass(type);
    }

    private static boolean shouldIntercept(Method method) {
        return !isGetIdMethod(method) && !isInitId(method) && !method.isAnnotationPresent(NoProxy.class)
                && !isObjectMethod(method);
    }

    private static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
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
