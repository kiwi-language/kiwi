package tech.metavm.entity;

import tech.metavm.object.instance.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class MockInstanceMap implements InstanceMap {

    private final Map<Object, IInstance> map = new HashMap<>();

    private final Function<Class<?>, ModelDef<?,?>> getModeDef;

    MockInstanceMap(Function<Class<?>, ModelDef<?, ?>> getModeDef) {
        this.getModeDef = getModeDef;
    }

    @Override
    public IInstance getByModel(Object model) {
        return map.computeIfAbsent(model, this::createRef);
    }

    private IInstance createRef(Object model) {
        ModelDef<?,?> modelDef = getModeDef.apply(model.getClass());
        Class<?> instanceType = modelDef.getInstanceType();
        if(IInstanceArray.class.isAssignableFrom(instanceType)) {
            return new InstanceArrayRef(
                    getId(model),
                    () -> (InstanceArray) modelDef.newInstanceHelper(model, this)
            );
        }
        else {
            return new InstanceRef(
                    getId(model),
                    modelDef.getEntityType(),
                    () -> (Instance) modelDef.newInstanceHelper(model, this)
            );
        }
    }

    private Long getId(Object model) {
        if(model instanceof Identifiable identifiable) {
            return identifiable.getId();
        }
        else {
            return null;
        }
    }


}
