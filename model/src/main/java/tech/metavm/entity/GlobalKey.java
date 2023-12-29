package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;

public interface GlobalKey {

    String getGlobalKey(@NotNull BuildKeyContext context);

    default boolean isValidGlobalKey() {
        return true;
    }

}
