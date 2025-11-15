package org.metavm.entity;

public class ModelDefRegistry {

    private static final DefContextHolder holder = new HybridDefContextHolder();

    public static void setDefContext(DefContext defContext) {
        holder.set(defContext);
    }

    public static DefContext getDefContext() {
        return holder.get();
    }

    public static boolean isDefContextPresent() {
        return holder.isPresent();
    }

    public static boolean isInitialized() {
        return holder.get() != null;
    }

}
