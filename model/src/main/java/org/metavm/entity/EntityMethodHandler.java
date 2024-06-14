package org.metavm.entity;

import javassist.util.proxy.MethodHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class EntityMethodHandler<T> implements MethodHandler {

    private static final boolean TRACE_INITIALIZATION = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMethodHandler.class);

    public enum State {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED
    }

    private final Class<T> type;
    private final Consumer<T> initializer;
    private State state = State.UNINITIALIZED;

    public EntityMethodHandler(Class<T> type, Consumer<T> initializer) {
        this.type = type;
        this.initializer = initializer;
    }


    public State getState() {
        return state;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        ensureInitialized(self, thisMethod);
        return proceed.invoke(self, args);
    }

    public void ensureInitialized(Object self) {
        ensureInitialized(self, null);
    }

    private void ensureInitialized(Object self, Method thisMethod) {
        if(isInitialized()) {
           return;
        }
        if(state == State.INITIALIZING) {
//            if(LOGGER.isDebugEnabled()) {
//                LOGGER.debug("Call method '" + ReflectUtils.getMethodQualifiedName(thisMethod) + "' while initializing proxy");
//            }
//            throw new InternalException(
//                    InternalErrorCode.PROXY_CIRCULAR_REF,
//                    thisMethod != null ? ReflectUtils.getMethodQualifiedName(thisMethod) : "null"
//            );
            return;
        }
        if(TRACE_INITIALIZATION) {
            LOGGER.info("Initializing proxy instance", new Exception());
        }
        state = State.INITIALIZING;
        initializer.accept(type.cast(self));
        state = State.INITIALIZED;
    }

    public boolean isInitialized() {
        return state == State.INITIALIZED;
    }

    public boolean isUninitialized() {
        return state == State.UNINITIALIZED;
    }

    public void setState(State state) {
        this.state = state;
    }

}
