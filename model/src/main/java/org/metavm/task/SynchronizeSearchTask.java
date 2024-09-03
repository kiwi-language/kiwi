package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.Identifier;
import org.metavm.util.Hooks;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class SynchronizeSearchTask extends Task {

    @ChildEntity
    private final ReadWriteArray<Identifier> changedIds = addChild(new ReadWriteArray<>(Identifier.class), "changedIds");
    @ChildEntity
    private final ReadWriteArray<Identifier> removedIds = addChild(new ReadWriteArray<>(Identifier.class), "removedIds");
    @ChildEntity
    @Nullable
    private final WAL wal;
    @Nullable
    private final Identifier defWalId;

    public SynchronizeSearchTask(List<Identifier> changedIds, List<Identifier> removedIds, @Nullable WAL wal, @Nullable Identifier defWalId) {
        super("SynchronizeSearchTask");
        this.changedIds.addAll(changedIds);
        this.removedIds.addAll(removedIds);
        this.wal = wal != null ? addChild(wal, "wal") : null;
        this.defWalId = defWalId;
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        var instanceContext = context.getInstanceContext();
        List<ClassInstance> changed = NncUtils.filterByType(instanceContext.batchGet(NncUtils.map(changedIds, Identifier::toId)), ClassInstance.class);
        try (var ignored = context.getProfiler().enter("bulk")) {
            Hooks.SEARCH_BULK.accept(context.getAppId(), changed, NncUtils.map(removedIds, Identifier::toId));
        }
        return true;
    }

    @Nullable
    @Override
    public WAL getMetaWAL() {
        return wal;
    }

    @Nullable
    @Override
    public Identifier getDefWalId() {
        return defWalId;
    }
}
