package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.util.NncUtils;

public class EntityTypeRegistry {

    private final InstanceContextFactory instanceContextFactory;

    private static DefContext DEF_CONTEXT;

    static void setDefContext(DefContext defContext) {
        NncUtils.requireNull(DEF_CONTEXT, () -> new IllegalStateException("DefContext already set"));
        DEF_CONTEXT = defContext;
    }

    public EntityTypeRegistry(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    public static Entity createEntity(Instance instance, ModelMap modelMap) {
        EntityDef<?> entityDef = (EntityDef<?>) DEF_CONTEXT.getDef(instance.getType().id);
        return entityDef.newModel(instance, modelMap);
    }

    public static Instance createInstance(Object entity, InstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(entity.getClass());
        return entityDef.newInstanceHelper(entity, instanceMap);
    }

    private static <T extends Entity> Instance newInstanceHelper(EntityDef<T> entityDef, Entity entity, InstanceMap instanceMap) {
        return entityDef.newInstance(entityDef.getEntityType().cast(entity), instanceMap);
    }

    public static void updateInstance(Object model, IInstance instance, InstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(instance.getType().id);
        updateInstanceHelper(entityDef, model, instance, instanceMap);
    }

    private static <T, I extends Instance> void updateInstanceHelper(
            ModelDef<T, I> modelDef,
            Object entity,
            IInstance instance,
            InstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getEntityType().cast(entity),
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

    public static Field getField(java.lang.reflect.Field javaField) {
        return DEF_CONTEXT.getPojoDef(javaField.getDeclaringClass()).getFieldDef(javaField).getField();
    }

    public static ConstraintPO getUniqueConstraint(IndexDef<?> def) {
        return null; // TODO to implement
    }

    public static Class<? extends Entity> getEntityType(long typeId) {
        return DEF_CONTEXT.getEntityDef(typeId).getEntityType();
    }

    public static Class<?> getJavaType(Type type) {
        return DEF_CONTEXT.getDef(type).getEntityType();
    }

    public static Class<? extends Enum<?>> getEnumType(long typeId) {
        return DEF_CONTEXT.getEnumDef(typeId).getEnumType();
    }

    public static <T extends Enum<?>> T getEnumConstant(Class<T> enumType, long id) {
        return enumType.cast(DEF_CONTEXT.getEnumDef(enumType).getEnumConstantDef(id).getValue());
    }

    public static EntityDef<?> getEntityDef(long typeId) {
        return DEF_CONTEXT.getEntityDef(typeId);
    }

    public InstanceContext newContext() {
        return instanceContextFactory.newContext();
    }

//    public static Enum<?> getEnumConstant(long id) {
//        return stdAllocators.getEnumConstantById(id);
//    }

}
