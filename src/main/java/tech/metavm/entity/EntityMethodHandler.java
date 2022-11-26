package tech.metavm.entity;

import javassist.util.proxy.MethodHandler;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;

import java.lang.reflect.Method;
import java.util.function.Function;

public final class EntityMethodHandler implements MethodHandler {

    private final Function<Instance, Object> modelCreator;
    private Object realEntity;
    private final IInstance instance;

    public EntityMethodHandler(IInstance instance, Function<Instance, Object> modelCreator) {
        this.instance = instance;
        this.modelCreator = modelCreator;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Object entity = getEntity();
        thisMethod.setAccessible(true);
        return thisMethod.invoke(entity, args);
    }

    public Object getEntity() {
        if(realEntity == null) {
            realEntity = modelCreator.apply(EntityContext.getRealInstance(instance));
        }
        return realEntity;
    }

}
