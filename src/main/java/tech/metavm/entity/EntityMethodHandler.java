package tech.metavm.entity;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class EntityMethodHandler<T> implements MethodHandler {

    private final Class<T> type;
    private final Consumer<T> initializer;
    private boolean initialized;

    public EntityMethodHandler(Class<T> type, Consumer<T> initializer) {
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        ensureInitialized(self);
        return proceed.invoke(self, args);
    }

    public void ensureInitialized(Object self) {
        if(initialized) {
           return;
        }
        initializer.accept(type.cast(self));
        initialized = true;
    }

}
