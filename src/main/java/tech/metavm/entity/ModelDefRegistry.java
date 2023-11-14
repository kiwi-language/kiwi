package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.util.ReflectUtils;

public class ModelDefRegistry {

    private final InstanceContextFactory instanceContextFactory;

    private static DefContext DEF_CONTEXT;

    public static void setDefContext(DefContext defContext) {
//        NncUtils.requireNull(DEF_CONTEXT, () -> new IllegalStateException("DefContext already set"));
        DEF_CONTEXT = defContext;
    }

    public ModelDefRegistry(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    public static boolean containsDef(Type type) {
        return DEF_CONTEXT.containsDef(type);
    }

    public static void setModelFields(Object model, Instance instance, ModelInstanceMap modelInstanceMap) {
        EntityDef<?> entityDef = (EntityDef<?>) DEF_CONTEXT.getDef(instance.getType());
        entityDef.initModelHelper(model, instance, modelInstanceMap);
    }

    public static Instance createInstance(Object entity, ModelInstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDefByModel(entity);
        return entityDef.createInstanceHelper(entity, instanceMap);
    }

    public static void updateInstance(Object model, Instance instance, ModelInstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = DEF_CONTEXT.getDef(instance.getType());
        updateInstanceHelper(entityDef, model, instance, instanceMap);
    }

    private static <T, I extends Instance> void updateInstanceHelper(
            ModelDef<T, I> modelDef,
            Object entity,
            Instance instance,
            ModelInstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getInstanceType().cast(instance), modelDef.getJavaClass().cast(entity),
                instanceMap
        );
    }

    public static Type getType(Entity entity) {
        return getType(EntityUtils.getRealType(entity.getClass()));
    }

    public static Type getType(Class<?> entityClass) {
        return DEF_CONTEXT.getDef(entityClass).getType();
    }

    public static Type getType(java.lang.reflect.Type javaType) {
        return DEF_CONTEXT.getDef(javaType).getType();
    }

    public static ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    public static long getTypeId(Class<?> entityClass) {
        return getType(entityClass).getId();
    }

    public static Field getField(java.lang.reflect.Field javaField) {
        return DEF_CONTEXT.getPojoDef(javaField.getDeclaringClass()).getFieldDef(javaField).getField();
    }

    public static Field getField(Class<?> klass, String fieldName) {
        return getField(ReflectUtils.getField(klass, fieldName));
    }

    public static Index getIndexConstraint(IndexDef<?> indexDef) {
        return DEF_CONTEXT.getIndexConstraint(indexDef);
    }

    public static Class<? extends Entity> getEntityType(ClassType type) {
        return DEF_CONTEXT.getEntityDef(type).getJavaClass();
    }

    public static java.lang.reflect.Type getJavaType(Type type) {
        return DEF_CONTEXT.getJavaType(type);
    }

    public static Class<?> getJavaClass(Type type) {
        return DEF_CONTEXT.getJavaClass(type);
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

    public static ModelDef<?,?> getDef(ClassType type) {
        return DEF_CONTEXT.getDef(type);
    }

    public IInstanceContext newContext() {
        return instanceContextFactory.newContext();
    }

    public static boolean isInitialized() {
        return DEF_CONTEXT != null;
    }

}
