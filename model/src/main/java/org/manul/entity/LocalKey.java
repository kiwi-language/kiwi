package org.manul.entity;

import org.jetbrains.annotations.NotNull;
import org.manul.api.JsonIgnore;

public interface LocalKey {

    @JsonIgnore
    boolean isValidLocalKey();

    @JsonIgnore
    String getLocalKey(@NotNull BuildKeyContext context);

}
