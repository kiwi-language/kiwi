package org.metavm.entity;

import org.metavm.object.instance.core.ContextListener;

public interface InstanceSink {

    void finish();

    void addListener(ContextListener listener);

}
