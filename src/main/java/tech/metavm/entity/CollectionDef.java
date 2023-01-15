package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CollectionDef<E, C extends Table<E>> extends ModelDef<C, ArrayInstance> {

    @SuppressWarnings("unchecked")
    static <E, C extends Table<E>> CollectionDef<?, ?> createHelper(Class<?> javaClass,
                                                                         java.lang.reflect.Type javaType,
                                                                         ModelDef<?,?> elementDef, ArrayType type) {
        if(javaType instanceof Class<?>) {
            NncUtils.requireEquals(Object.class, elementDef.getJavaClass(),
                    "For a raw collection def, element type should be object");
        }
        else if(javaType instanceof ParameterizedType parameterizedType){
            java.lang.reflect.Type elementType = parameterizedType.getActualTypeArguments()[0];
            NncUtils.requireEquals(
                    elementType, elementDef.getJavaType(),
                    "Element type mismatch. " +
                            "Element type from java type: " + elementType + ", " +
                            "Type of elementDef: " + elementDef.getJavaType());
        }
        else {
            throw new InternalException("Invalid java type '" + javaType + "' for collection def");
        }
        CollectionDef<E, C>  def = new CollectionDef<>((Class<C>) javaClass, javaType, type, (ModelDef<E, ?>) elementDef);
        return def;
    }

    private final ArrayType type;
    private final ModelDef<E, ?> elementDef;

    public CollectionDef(Class<C> javaClass, java.lang.reflect.Type javaType, ArrayType type, ModelDef<E, ?> elementDef) {
        super(javaClass, javaType, ArrayInstance.class);
        this.elementDef = elementDef;
        this.type = type;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        if(isProxySupported()) {
            return ReflectUtils.invokeConstructor(
                    ReflectUtils.getConstructor(proxyClass, java.lang.reflect.Type.class),
                    elementDef.getJavaType()
            );
        }
        else {
            return ReflectUtils.invokeConstructor(ReflectUtils.getConstructor(proxyClass));
        }
    }

    @Override
    protected C allocateModel() {
        return ReflectUtils.invokeConstructor(ReflectUtils.getConstructor(getJavaClass()));
    }

    @Override
    public void initModel(C model, ArrayInstance instance, ModelInstanceMap modelInstanceMap) {
        model.setElementAsChild(instance.isElementAsChild());
        model.addAll(
                NncUtils.map(
                        instance.getElements(),
                        e -> modelInstanceMap.getModel(elementDef.getJavaClass(), e)
                )
        );
    }

    @Override
    public void updateModel(C model, ArrayInstance instance, ModelInstanceMap modelInstanceMap) {
        model.clear();
        model.setElementAsChild(instance.isElementAsChild());
        initModel(model, instance, modelInstanceMap);
    }

    @Override
    public void initInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.setElementAsChild(model.isElementAsChild());
        if(elementDef instanceof InstanceDef<?>) {
            for (E e : model) {
                instance.add(elementDef.getInstanceType().cast(e));
            }
        }
        else {
            instance.addAll(NncUtils.map(new ArrayList<>(model), instanceMap::getInstance));
        }
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.clear();
        instance.setElementAsChild(model.isElementAsChild());
        initInstance(instance, model, instanceMap);
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of(getJavaType(), type);
    }

    @Override
    public boolean isProxySupported() {
        return RuntimeGeneric.class.isAssignableFrom(getJavaClass());
    }

}
