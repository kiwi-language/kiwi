package org.metavm.entity;

import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.RuntimeGeneric;

import javax.annotation.Nullable;

public class ModelDefRegistry {

    private static DefContextHolder holder = new HybridDefContextHolder();

    public static void setHolder(DefContextHolder holder) {
        ModelDefRegistry.holder = holder;
    }

    public static void setDefContext(DefContext defContext) {
//        NncUtils.requireNull(holder.get(), () -> new IllegalStateException("DefContext already set"));
        holder.set(defContext);
    }

    public static void setLocalDefContext(DefContext defContext) {
        ((HybridDefContextHolder) holder).setLocal(defContext);
    }

    public static DefContext getDefContext() {
        return holder.get();
    }

    public static void clearLocal() {
        holder.clearLocal();
    }

    public static boolean isDefContextPresent() {
        return holder.isPresent();
    }

    public static boolean containsDef(TypeDef typeDef) {
        return getDefContext().containsDef(typeDef);
    }

    private static <T, I extends ClassInstance> void updateInstanceHelper(
            ModelDef<T> modelDef,
            Object entity,
            Value instance,
            ObjectInstanceMap instanceMap) {
        modelDef.updateInstance(
                modelDef.getInstanceClass().cast(instance), modelDef.getEntityClass().cast(entity),
                instanceMap
        );
    }

    public static Type getType(Entity entity) {
        if (entity instanceof RuntimeGeneric runtimeGeneric)
            return getType(runtimeGeneric.getGenericType());
        else
            return getType(EntityUtils.getRealType(entity.getClass()));
    }

    public static Type getType(Class<?> entityClass) {
        return holder.get().getType(entityClass);
    }

    public static Type getType(java.lang.reflect.Type javaType) {
        return holder.get().getType(javaType);
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
        return ((EntityDef<?>) holder.get().getDef(type.resolve())).getEntityClass();
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

    public static <T> ModelDef<T> getDef(Class<T> aClass) {
        return holder.get().getDef(aClass);
    }

    public static @Nullable ModelDef<?> tryGetDef(TypeDef typeDef) {
        return holder.get().tryGetDef(typeDef);
    }

    public static boolean isInitialized() {
        return holder.get() != null;
    }

}
