package org.manul.entity;

import org.manul.object.instance.core.ContextListener;

public interface InstanceSink {

    void finish();

    void addListener(ContextListener listener);

}
