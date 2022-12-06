package tech.metavm.entity;

import tech.metavm.object.instance.Instance;

import java.util.Collection;
import java.util.List;

public interface InstanceSink {

    default void replace(Instance instance) {
        replace(List.of(instance));
    }

    void replace(Collection<Instance> instances);

    void finish();

    void initIds();

    void addListener(InstanceSinkListener listener);

}
