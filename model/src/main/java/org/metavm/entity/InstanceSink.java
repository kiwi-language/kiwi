package org.metavm.entity;

import org.metavm.object.instance.core.ContextListener;

public interface InstanceSink {

    void finish();

    void initIds();

    void addListener(ContextListener listener);

}
