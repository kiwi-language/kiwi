package org.manul.entity;

import org.manul.api.JsonIgnore;
import org.manul.object.instance.core.IInstanceContext;

public interface ChangeAware {

    void onChange(IInstanceContext context);

    @JsonIgnore
    default boolean isChangeAware() {
        return true;
    }

}
