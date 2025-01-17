package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;

public interface BindAware {

    /**
     * Invoked when this entity is bound to the context
     */
    void onBind(IInstanceContext context);

}
