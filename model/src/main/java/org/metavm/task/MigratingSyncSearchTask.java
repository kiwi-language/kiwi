package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;

import java.util.Collection;

// This is a workaround for the fact that we can't evolve system classes right now
@NativeEntity(102)
@Slf4j
public class MigratingSyncSearchTask extends SyncSearchTask {
    public MigratingSyncSearchTask(Id id, Collection<Id> changedIds, Collection<Id> removedIds) {
        super(id, changedIds, removedIds);
        log.debug("Creating migrating sync search task, changedIds: {}, removedIds: {}", changedIds, removedIds, new Exception());
    }

    @Override
    public boolean isMigrating() {
        return true;
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        return super.run1(context, taskContext);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_MigratingSearchSyncTask;
    }
}
