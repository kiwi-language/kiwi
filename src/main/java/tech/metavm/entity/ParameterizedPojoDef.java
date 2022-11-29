package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public class ParameterizedPojoDef<T> extends ModelDef<T, Instance>{

    private PojoDef<? super T> rawTypeDef;
    private Type type;
    private ParameterizedType parameterizedType;

    protected ParameterizedPojoDef(Class<T> entityType, Class<Instance> instanceType) {
        super(entityType, instanceType);
    }

    protected ParameterizedPojoDef(TypeReference<T> typeReference, Class<Instance> instanceType) {
        super(typeReference, instanceType);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public T newModel(Instance instance, ModelMap modelMap) {
        return getEntityType().cast(rawTypeDef.newModel(instance, modelMap));
    }

    @Override
    public void updateModel(T model, Instance instance, ModelMap modelMap) {
        rawTypeDef.updateModel(model, instance, modelMap);
    }

    @Override
    public Instance newInstance(T model, InstanceMap instanceMap) {
        return new Instance(
                rawTypeDef.getInstanceFields(model, instanceMap),
                type,
                getEntityType()
        );
    }

    @Override
    public void updateInstance(T model, Instance instance, InstanceMap instanceMap) {
        rawTypeDef.updateInstance(model, instance, instanceMap);
    }

    @Override
    public Map<Object, Entity> getEntityMapping() {
        return null;
    }
}
