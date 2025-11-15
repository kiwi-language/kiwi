package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.search.SearchSync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Entity
@Wire(73)
@Slf4j
public class SyncSearchTask extends Task {

    private final List<Id> changedIds = new ArrayList<>();
    private final List<Id> removedIds = new ArrayList<>();

    public SyncSearchTask(Id id, Collection<Id> changedIds, Collection<Id> removedIds) {
        super(id, "SyncSearchTask");
        this.changedIds.addAll(changedIds);
        this.removedIds.addAll(removedIds);
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        SearchSync.sync(changedIds, removedIds, false, context);
        return true;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
