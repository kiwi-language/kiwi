package tech.metavm.object.instance.core;

public interface ContextListener {

    default void beforeFinish() {}

    default void onInstanceInitialized(DurableInstance instance) {}

    default void onInstanceRemoved(DurableInstance instance) {}

    default void onInstanceIdInit(DurableInstance instance) {}

    default void afterContextIntIds() {}

    default void onPatchBuild() {}

    default boolean onChange(Instance instance) {
        return false;
    }

}
