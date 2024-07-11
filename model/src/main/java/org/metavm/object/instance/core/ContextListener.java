package org.metavm.object.instance.core;

public interface ContextListener {

    default void beforeFinish() {}

    /**
     * Notifies the listener that the instance has been initialized. Only root instances are notified.
     */
    default void onInstanceInitialized(DurableInstance instance) {}

    /**
     * The implementation shall not make changes to entities or instances in the contexts.
     * If changes are made, the changes may not be saved.
     */
    default void onInstanceRemoved(DurableInstance instance) {
    }

    default void onInstanceIdInit(DurableInstance instance) {}

    default void afterContextIntIds() {}

    default void onPatchBuild() {}

    default boolean onChange(DurableInstance instance) {
        return false;
    }

    default boolean onRemove(DurableInstance instance) {
        return false;
    }

}
