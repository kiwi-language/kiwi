package tech.metavm.entity;

import tech.metavm.object.instance.core.ContextListener;

public interface InstanceSink {

    void finish();

    void initIds();

    void addListener(ContextListener listener);

}
