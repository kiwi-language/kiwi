package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;

public interface PostRemovalAware {

    void postRemove(IInstanceContext context);

}
