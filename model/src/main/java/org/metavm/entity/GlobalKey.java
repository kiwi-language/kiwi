package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.JsonIgnore;

public interface GlobalKey {

    @JsonIgnore
    String getGlobalKey(@NotNull BuildKeyContext context);

    @JsonIgnore
    default boolean isValidGlobalKey() {
        return true;
    }

}
