package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.InternalException;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.RuntimeGeneric;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public class InstanceArrayMapper<E extends Instance, C extends ReadWriteArray<E>> implements Mapper<C, ArrayInstance> {

    public static InstanceArrayMapper<?,?> create(Class<?> javaClass, java.lang.reflect.Type javaType, Class<?> elementClass, Type type) {
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
                    return new InstanceArrayMapper(
                            javaClass, parameterizedType, elementClass, arrayType
                    );
                }
            }
        }
        throw new InternalException("Fail to create InstanceArrayMapper with arguments (" +
                javaClass + ", " + javaType  + ", " + elementClass + ", " + type + ")"
        );
    }

    private final ArrayType type;
    private final Class<C> entityClass;
    private final ParameterizedType entityType;
    private final Class<E> elementClass;

    public InstanceArrayMapper(Class<C> entityClass, ParameterizedType entityType, Class<E> elementClass, ArrayType type) {
        this.elementClass = elementClass;
        this.entityClass = entityClass;
        this.entityType = entityType;
        this.type = type;
    }

    @Override
    public Class<C> getEntityClass() {
        return entityClass;
    }

    public ParameterizedType getEntityType() {
        return entityType;
    }

    @Override
    public Class<ArrayInstance> getInstanceClass() {
        return ArrayInstance.class;
    }

    public ArrayType getType() {
        return type;
    }

    @Override
    public void initEntity(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
        for (Instance element : instance) {
            model.add(elementClass.cast(element));
        }
    }

    @Override
    public void updateEntity(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
        model.clear();
        initEntity(model, instance, objectInstanceMap);
    }

    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        if(isProxySupported()) {
            return ReflectionUtils.invokeConstructor(
                    ReflectionUtils.getConstructor(proxyClass, java.lang.reflect.Type.class),
                    elementClass
            );
        }
        else {
            return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(proxyClass));
        }
    }

    @Override
    public C allocateEntity() {
        return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(getEntityClass()));
    }

    @Override
    public void initInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        instance.setType(instanceMap.getType(EntityUtils.getRuntimeType(model)));
        instance.addAll(model);
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        instance.clear();
        instance.addAll(model);
    }

    @Override
    public boolean isProxySupported() {
        return RuntimeGeneric.class.isAssignableFrom(getEntityClass());
    }

}
