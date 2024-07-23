package org.metavm.task;

import org.metavm.object.instance.core.WAL;

import javax.annotation.Nullable;

public interface WalTask {

    @Nullable WAL getWAL();

    default boolean isMigrationEnabled() {
        return false;
    }

}
