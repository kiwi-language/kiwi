package tech.metavm.entity;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.AnyType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.RuntimeGeneric;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

public class InstanceCollectionDef<E extends Instance, C extends Collection<E>> extends ModelDef<C, ArrayInstance> {

    public static InstanceCollectionDef<?,?> create(Class<?> javaClass, java.lang.reflect.Type javaType,
                                                    Class<?> elementClass, Type type) {
        if(javaType instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            if(Collection.class.isAssignableFrom(rawClass)
                    && rawClass == javaClass
                    && parameterizedType.getActualTypeArguments().length == 1) {
                java.lang.reflect.Type elementType = parameterizedType.getActualTypeArguments()[0];
                if(Instance.class.isAssignableFrom(elementClass)
                        && elementType == elementClass
                        && (type instanceof ArrayType arrayType)
                        && (arrayType.getElementType() instanceof AnyType)) {
                    //noinspection unchecked,rawtypes
                    return new InstanceCollectionDef(
                            javaClass, javaType, elementClass, type
                    );
                }
            }
        }
        throw new InternalException("Fail to create InstanceCollectionDef with arguments (" +
                javaClass + ", " + javaType  + ", " + elementClass + ", " + type + ")"
        );
    }

    private final Type type;
    private final Class<E> elementClass;

    private InstanceCollectionDef(Class<C> javaClass, java.lang.reflect.Type javaType,
                                    Class<E> elementClass, Type type) {
        super(javaClass, javaType, ArrayInstance.class);
        this.elementClass = elementClass;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void initModel(C model, ArrayInstance instance, ModelInstanceMap modelInstanceMap) {
        for (Instance element : instance) {
            model.add(elementClass.cast(element));
        }
    }

    @Override
    public void updateModel(C model, ArrayInstance instance, ModelInstanceMap modelInstanceMap) {
        model.clear();
        initModel(model, instance, modelInstanceMap);
    }


    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        if(isProxySupported()) {
            return ReflectUtils.invokeConstructor(
                    ReflectUtils.getConstructor(proxyClass, java.lang.reflect.Type.class),
                    elementClass
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
    public void initInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.addAll(model);
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ModelInstanceMap instanceMap) {
        instance.clear();
        instance.addAll(model);
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return Map.of();
    }

    @Override
    public boolean isProxySupported() {
        return RuntimeGeneric.class.isAssignableFrom(getJavaClass());
    }

}
