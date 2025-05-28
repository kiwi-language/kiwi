package org.metavm.object.instance.core;

import java.util.List;
import java.util.function.Predicate;

public interface ContextListener {

    default void beforeFinish() {}

    /**
     * Notifies the listener that the instance has been initialized. Only root instances are notified.
     */
    default void onInstanceInitialized(Instance instance) {}

    /**
     * The implementation shall not make changes to entities or instances in the contexts.
     * If changes are made, the changes may not be saved.
     */
    default void onInstanceRemoved(Instance instance) {
    }

    default void onInstanceIdInit(Instance instance) {}

    default boolean onChange(Instance instance) {
        return false;
    }

    default boolean onRemove(Instance instance) {
        return false;
    }

}
