package org.metavm.entity;

import org.metavm.api.JsonIgnore;

public interface ChangeAware {

    void onChange(IEntityContext context);

    @JsonIgnore
    default boolean isChangeAware() {
        return true;
    }

}
