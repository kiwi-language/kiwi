package tech.metavm.object.instance.core;

public interface ContextListener {

    default void beforeFinish() {}

    default void onInstanceInitialized(Instance instance) {}

    default void onInstanceRemoved(Instance instance) {}

    default void onInstanceIdInit(Instance instance) {}

}
