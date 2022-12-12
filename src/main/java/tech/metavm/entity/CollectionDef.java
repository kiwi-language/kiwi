package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.RuntimeGeneric;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

public class CollectionDef<E, C extends Collection<E>> extends ModelDef<C, ArrayInstance> {

    @SuppressWarnings("unchecked")
    static <E, C extends Collection<E>> CollectionDef<?, ?> createHelper(Class<?> javaClass,
                                                                         java.lang.reflect.Type javaType,
                                                                         ModelDef<?,?> elementDef, Type type) {
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
        return new CollectionDef<>((Class<C>) javaClass, javaType, (ModelDef<E, ?>) elementDef, type);
    }

    private final Type type;
    private final ModelDef<E, ?> elementDef;

    public CollectionDef(Class<C> javaClass, java.lang.reflect.Type javaType, ModelDef<E, ?> elementDef, Type type) {
        super(javaClass, javaType, ArrayInstance.class);
        this.elementDef = elementDef;
        this.type = type;
    }

    @Override
    public Type getType() {
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
        initModel(model, instance, modelInstanceMap);
    }

    @Override
    public void initInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.addAll(NncUtils.map(model, instanceMap::getInstance));
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.clear();
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
