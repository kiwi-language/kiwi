package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.JsonIgnore;

public interface LocalKey {

    @JsonIgnore
    boolean isValidLocalKey();

    @JsonIgnore
    String getLocalKey(@NotNull BuildKeyContext context);

}
