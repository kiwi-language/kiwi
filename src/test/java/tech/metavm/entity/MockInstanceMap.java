package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class MockInstanceMap implements InstanceMap {

    private final Map<Object, Instance> map = new HashMap<>();

    private final Function<Class<?>, ModelDef<?,?>> getModeDef;

    MockInstanceMap(Function<Class<?>, ModelDef<?, ?>> getModeDef) {
        this.getModeDef = getModeDef;
    }

    @Override
    public Instance getInstanceByModel(Object model) {
        return map.computeIfAbsent(model, this::getInstance);
    }

    private Instance getInstance(Object model) {
        ModelDef<?,?> modelDef = getModeDef.apply(model.getClass());
        return getInstanceRef(model, modelDef);
    }

    private <I extends Instance> I getInstanceRef(Object model, ModelDef<?, I> modelDef) {
        return EntityProxyFactory.getProxyInstance(
                modelDef.getInstanceType(),
                () -> modelDef.getInstanceType().cast(modelDef.newInstanceHelper(model, this))
        );
    }

}
