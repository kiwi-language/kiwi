package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.WAL;

public interface WalTask {

    WAL getWAL();

    void setTaskContext(IEntityContext context);

    default boolean isMigrationDisabled() {
        return false;
    }

}
