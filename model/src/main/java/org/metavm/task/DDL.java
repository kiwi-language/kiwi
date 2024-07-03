package org.metavm.task;

import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DDL extends ScanTask implements WalTask {

    private static final Logger logger = LoggerFactory.getLogger(DDL.class);

    private final Commit commit;

    public DDL(Commit commit) {
        super("DDL " + NncUtils.formatDate(commit.getTime()));
        this.commit = commit;
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context) {
        Instances.applyDDL(
                () -> batch.stream()
                        .filter(i -> i instanceof ClassInstance)
                        .map(i -> (ClassInstance) i)
                        .iterator(),
                commit, context);
    }

    @Override
    protected void onScanOver(IEntityContext context) {
        commit.finish();
    }

    @Override
    public WAL getWAL() {
        return commit.getWal();
    }
}
