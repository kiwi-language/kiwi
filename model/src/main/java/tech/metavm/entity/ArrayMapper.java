package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ArrayMapper<E, C extends ReadonlyArray<E>> implements Mapper<C, ArrayInstance> {

    private final Class<C> entityClass;
    private final ParameterizedType entityType;
    private final Class<E> elementClass;
    private final ArrayType type;
    private final DefContext defContext;

    public ArrayMapper(Class<C> entityClass, ParameterizedType entityType, Class<E> elementClass, ArrayType type, DefContext defContext) {
        this.entityClass = entityClass;
        this.entityType = entityType;
        this.type = type;
        this.elementClass = elementClass;
        this.defContext = defContext;
    }

    @Override
    public C allocateEntity() {
        return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(getEntityClass()));
    }

    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        if (isProxySupported()) {
            return ReflectionUtils.invokeConstructor(
                    ReflectionUtils.getConstructor(proxyClass, Type.class),
                    entityType.getActualTypeArguments()[0]
            );
        } else
            return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(proxyClass));
    }

    @Override
    public void initEntity(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
        model.initialize(
                NncUtils.map(
                        instance.getElements(),
                        e -> objectInstanceMap.getEntity(elementClass, e)
                )
        );
        model.setParent(
                NncUtils.get(instance.getParent(), p -> objectInstanceMap.getEntity(Entity.class, p)),
                NncUtils.get(instance.getParentField(), defContext::getJavaField));
    }

    @Override
    public void updateEntity(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
//        model.clear();
        initEntity(model, instance, objectInstanceMap);
    }

    @Override
    public Class<C> getEntityClass() {
        return entityClass;
    }

    @Override
    public Type getEntityType() {
        return entityType;
    }

    @Override
    public Class<ArrayInstance> getInstanceClass() {
        return ArrayInstance.class;
    }

    @Override
    public boolean isProxySupported() {
        return true;
    }

    @Override
    public void initInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        resetInstance(instance, model, instanceMap);
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        instance.ensureLoaded();
        resetInstance(instance, model, instanceMap);
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    private void resetInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        Instances.reloadParent(model, instance, instanceMap, defContext);
        instance.reset(NncUtils.map(NncUtils.listOf(model), e -> e instanceof Instance i ? i : instanceMap.getInstance(e)));
    }

}
