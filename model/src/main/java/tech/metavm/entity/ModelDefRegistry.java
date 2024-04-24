package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.*;
import tech.metavm.object.type.Index;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.RuntimeGeneric;

import javax.annotation.Nullable;

public class ModelDefRegistry {

    private static DefContextHolder holder = new GlobalDefContextHolder();

    public static void setHolder(DefContextHolder holder) {
        ModelDefRegistry.holder = holder;
    }

    public static void setDefContext(DefContext defContext) {
//        NncUtils.requireNull(holder.get(), () -> new IllegalStateException("DefContext already set"));
        holder.set(defContext);
    }

    public static DefContext getDefContext() {
        return holder.get();
    }

    public static boolean isDefContextPresent() {
        return holder.isPresent();
    }

    public static boolean containsDef(Type type) {
        return getDefContext().containsDef(type);
    }

    public static void setModelFields(Object model, Instance instance, ObjectInstanceMap objectInstanceMap) {
        EntityDef<?> entityDef = (EntityDef<?>) getDefContext().getDef(instance.getType());
        entityDef.initModelHelper(model, instance, objectInstanceMap);
    }

    public static void updateInstance(Object model, Instance instance, ObjectInstanceMap instanceMap) {
        ModelDef<?, ?> entityDef = getDefContext().getDef(instance.getType());
        updateInstanceHelper(entityDef, model, instance, instanceMap);
    }

    private static <T, I extends DurableInstance> void updateInstanceHelper(
            ModelDef<T, I> modelDef,
            Object entity,
            Instance instance,
            ObjectInstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getInstanceType().cast(instance), modelDef.getJavaClass().cast(entity),
                instanceMap
        );
    }

    public static Type getType(Entity entity) {
        if(entity instanceof RuntimeGeneric runtimeGeneric)
            return getType(runtimeGeneric.getGenericType());
        else
            return getType(EntityUtils.getRealType(entity.getClass()));
    }

    public static Type getType(Class<?> entityClass) {
        return holder.get().getDef(entityClass).getType();
    }

    public static Type getType(java.lang.reflect.Type javaType) {
        return holder.get().getDef(javaType).getType();
    }

    public static ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    public static String getTypeId(Class<?> entityClass) {
        return getType(entityClass).getStringId();
    }

    public static Field getField(java.lang.reflect.Field javaField) {
        return holder.get().getPojoDef(javaField.getDeclaringClass()).getFieldDef(javaField).getField();
    }

    public static Field getField(Class<?> klass, String fieldName) {
        return getField(ReflectionUtils.getField(klass, fieldName));
    }

    public static Index getIndexConstraint(IndexDef<?> indexDef) {
        return holder.get().getIndexConstraint(indexDef);
    }

    public static Class<? extends Entity> getEntityType(ClassType type) {
        return holder.get().getEntityDef(type).getJavaClass();
    }

    public static java.lang.reflect.Type getJavaType(Type type) {
        return holder.get().getJavaType(type);
    }

    public static Class<?> getJavaClass(Type type) {
        return holder.get().getJavaClass(type);
    }

    public static <T extends Enum<?>> T getEnumConstant(Class<T> enumType, long id) {
        return enumType.cast(holder.get().getEnumDef(enumType).getEnumConstantDef(id).getValue());
    }

    public static EntityDef<?> getEntityDef(Type type) {
        return holder.get().getEntityDef(type);
    }

    public static <T> ModelDef<T,?> getDef(Class<T> aClass) {
        return holder.get().getDef(aClass);
    }

    public static ModelDef<?,?> getDef(ClassType type) {
        return holder.get().getDef(type);
    }

    public static @Nullable  ModelDef<?,?> tryGetDef(ClassType type) {
        return holder.get().tryGetDef(type);
    }

    public static boolean isInitialized() {
        return holder.get() != null;
    }

}
