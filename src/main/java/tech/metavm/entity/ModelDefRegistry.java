package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.object.meta.persistence.ConstraintPO;

public class ModelDefRegistry {

    private final InstanceContextFactory instanceContextFactory;

    private static DefContext DEF_CONTEXT;

    static void setDefContext(DefContext defContext) {
//        NncUtils.requireNull(DEF_CONTEXT, () -> new IllegalStateException("DefContext already set"));
        DEF_CONTEXT = defContext;
    }

    public ModelDefRegistry(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    public static void setModelFields(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {
        EntityDef<?> entityDef = (EntityDef<?>) DEF_CONTEXT.getDef(instance.getType());
        entityDef.initModelHelper(model, instance, modelInstanceMap);
    }

    public static Instance createInstance(Object entity, ModelInstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(entity.getClass());
        return entityDef.createInstanceHelper(entity, instanceMap);
    }

    public static void updateInstance(Object model, IInstance instance, ModelInstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(instance.getType());
        updateInstanceHelper(entityDef, model, instance, instanceMap);
    }

    private static <T, I extends Instance> void updateInstanceHelper(
            ModelDef<T, I> modelDef,
            Object entity,
            IInstance instance,
            ModelInstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getModelType().cast(entity),
                modelDef.getInstanceType().cast(instance),
                instanceMap
        );
    }

    public static Type getType(Entity entity) {
        return getType(EntityUtils.getRealType(entity.getClass()));
    }

    public static Type getType(Class<?> entityClass) {
        return DEF_CONTEXT.getDef(entityClass).getType();
    }

    public static long getTypeId(Class<?> entityClass) {
        return getType(entityClass).getId();
    }

    public static Field getField(java.lang.reflect.Field javaField) {
        return DEF_CONTEXT.getPojoDef(javaField.getDeclaringClass()).getFieldDef(javaField).getField();
    }

    public static UniqueConstraintRT getUniqueConstraint(IndexDef<?> indexDef) {
        return DEF_CONTEXT.getUniqueConstraint(indexDef);
    }

    public static Class<? extends Entity> getEntityType(Type type) {
        return DEF_CONTEXT.getEntityDef(type).getModelType();
    }

    public static Class<?> getJavaType(Type type) {
        return DEF_CONTEXT.getDef(type).getModelType();
    }

    public static <T extends Enum<?>> T getEnumConstant(Class<T> enumType, long id) {
        return enumType.cast(DEF_CONTEXT.getEnumDef(enumType).getEnumConstantDef(id).getValue());
    }

    public static EntityDef<?> getEntityDef(Type type) {
        return DEF_CONTEXT.getEntityDef(type);
    }

    public static <T> ModelDef<T,?> getDef(Class<T> aClass) {
        return DEF_CONTEXT.getDef(aClass);
    }

    public static ModelDef<?,?> getDef(Type type) {
        return DEF_CONTEXT.getDef(type);
    }

    public InstanceContext newContext() {
        return instanceContextFactory.newContext();
    }

    public static boolean isInitialized() {
        return DEF_CONTEXT != null;
    }

}
