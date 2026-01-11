package org.manul.entity;

import org.manul.object.instance.core.IInstanceContext;

public interface PostRemovalAware {

    void postRemove(IInstanceContext context);

}
