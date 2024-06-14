package org.metavm.entity;

import org.metavm.object.instance.core.ClassInstance;

public interface ChangeAware {

    void onChange(ClassInstance instance, IEntityContext context);

    default boolean isChangeAware() {
        return true;
    }

}
