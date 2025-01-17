package org.metavm.entity;

import org.metavm.object.type.*;
import org.metavm.util.RuntimeGeneric;

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

    public static boolean isInitialized() {
        return holder.get() != null;
    }

}
