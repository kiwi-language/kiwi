package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;

public class ArrayMapper<E, C extends ReadonlyArray<E>> implements Mapper<C, ArrayInstance> {

    private final Class<C> entityClass;
    private final DefContext defContext;

    public ArrayMapper(Class<C> entityClass, DefContext defContext) {
        this.entityClass = entityClass;
        this.defContext = defContext;
    }

    @Override
    public C allocateEntity() {
        return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(getEntityClass()));
    }

    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(proxyClass));
    }

    @Override
    public void initEntity(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
        //noinspection unchecked
        model.initialize(
                (ParameterizedType) defContext.getJavaType(instance.getType()),
                NncUtils.map(
                        instance.getElements(),
                        e -> (E) objectInstanceMap.getEntity(Object.class, e)
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

    private void resetInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        Instances.reloadParent(model, instance, instanceMap, defContext);
        instance.reset(NncUtils.map(NncUtils.listOf(model), e -> e instanceof Instance i ? i : instanceMap.getInstance(e)));
    }

}
