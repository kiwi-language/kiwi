package org.metavm.entity;

import org.metavm.api.JsonIgnore;
import org.metavm.object.instance.core.IInstanceContext;

public interface ChangeAware {

    void onChange(IInstanceContext context);

    @JsonIgnore
    default boolean isChangeAware() {
        return true;
    }

}
