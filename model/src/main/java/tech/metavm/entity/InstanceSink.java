package tech.metavm.entity;

import tech.metavm.object.instance.core.ContextListener;
import tech.metavm.object.instance.core.DurableInstance;

import java.util.Collection;
import java.util.List;

public interface InstanceSink {

    default void replace(DurableInstance instance) {
        replace(List.of(instance));
    }

    void replace(Collection<DurableInstance> instances);

    void finish();

    void initIds();

    void addListener(ContextListener listener);

}
