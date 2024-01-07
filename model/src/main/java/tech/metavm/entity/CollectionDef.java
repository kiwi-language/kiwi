package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.RuntimeGeneric;

import java.lang.reflect.Type;

public class CollectionDef<E, C extends ReadonlyArray<E>> extends ModelDef<C, ArrayInstance> {

    private final ArrayType type;
    private final ModelDef<E, ?> elementDef;
    private final DefContext defContext;

    public CollectionDef(Class<C> javaClass, Type javaType, ArrayType type, ModelDef<E, ?> elementDef, DefContext defContext) {
        super(javaClass, javaType, ArrayInstance.class);
        this.elementDef = elementDef;
        this.type = type;
        this.defContext = defContext;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    @Override
    public C createModelProxy(Class<? extends C> proxyClass) {
        if (isProxySupported()) {
            return ReflectionUtils.invokeConstructor(
                    ReflectionUtils.getConstructor(proxyClass, Type.class),
                    elementDef.getJavaType()
            );
        } else {
            return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(proxyClass));
        }
    }

    @Override
    protected C allocateModel() {
        return ReflectionUtils.invokeConstructor(ReflectionUtils.getConstructor(getJavaClass()));
    }

    @Override
    public void initModel(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
        model.initialize(
                NncUtils.map(
                        instance.getElements(),
                        e -> objectInstanceMap.getEntity(elementDef.getJavaClass(), e)
                )
        );
        model.setParent(
                NncUtils.get(instance.getParent(), p -> objectInstanceMap.getEntity(Entity.class, p)),
                NncUtils.get(instance.getParentField(), defContext::getJavaField));
    }

    @Override
    public void updateModel(C model, ArrayInstance instance, ObjectInstanceMap objectInstanceMap) {
//        model.clear();
        initModel(model, instance, objectInstanceMap);
    }

    @Override
    public void initInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        reloadParent(model, instance, instanceMap, defContext);
        if (elementDef instanceof InstanceDef<?>) {
            instance.reset(NncUtils.map(model, e -> elementDef.getInstanceType().cast(e)));
        } else {
            instance.reset(NncUtils.map(NncUtils.listOf(model), instanceMap::getInstance));
        }
    }

    @Override
    public void updateInstance(ArrayInstance instance, C model, ObjectInstanceMap instanceMap) {
        initInstance(instance, model, instanceMap);
    }

    @Override
    public boolean isProxySupported() {
        return RuntimeGeneric.class.isAssignableFrom(getJavaClass());
    }

}
