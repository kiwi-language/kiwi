package org.metavm.entity;

import org.jetbrains.annotations.NotNull;

public interface LocalKey {

    boolean isValidLocalKey();

    String getLocalKey(@NotNull BuildKeyContext context);

}
