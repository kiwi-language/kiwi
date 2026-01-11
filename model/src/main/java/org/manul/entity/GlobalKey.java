package org.manul.entity;

import org.jetbrains.annotations.NotNull;
import org.manul.api.JsonIgnore;

public interface GlobalKey {

    @JsonIgnore
    String getGlobalKey(@NotNull BuildKeyContext context);

    @JsonIgnore
    default boolean isValidGlobalKey() {
        return true;
    }

}
