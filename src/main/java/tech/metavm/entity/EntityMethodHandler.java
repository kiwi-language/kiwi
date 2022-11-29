package tech.metavm.entity;

import javassist.util.proxy.MethodHandler;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public final class EntityMethodHandler implements MethodHandler {

    private final Supplier<?> modelCreator;
    private Object realEntity;

    public EntityMethodHandler(Supplier<?> modelSupplier) {
        this.modelCreator = modelSupplier;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Object entity = getEntity();
        thisMethod.setAccessible(true);
        return thisMethod.invoke(entity, args);
    }

    public Object getEntity() {
        if(realEntity == null) {
            realEntity = modelCreator.get();
        }
        return realEntity;
    }

}
