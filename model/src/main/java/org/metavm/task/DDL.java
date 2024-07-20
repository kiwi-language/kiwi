package org.metavm.task;

import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DDL extends ScanTask implements WalTask, ParentTask {

    private static final Logger logger = LoggerFactory.getLogger(DDL.class);

    private int activeSubTaskCount;
    private final Commit commit;
    private transient IEntityContext taskContext;

    public DDL(Commit commit) {
        super("DDL " + NncUtils.formatDate(commit.getTime()));
        this.commit = commit;
    }

    @Override
    protected List<InstanceReference> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<InstanceReference> batch, IEntityContext context) {
        var tasks = Instances.applyDDL(
                () -> batch.stream()
                        .map(InstanceReference::resolve)
                        .filter(i -> i instanceof ClassInstance)
                        .map(i -> (ClassInstance) i)
                        .iterator(),
                commit, context);
        for (Task task : tasks) {
            taskContext.bind(task);
            if(task instanceof SubTask subTask) {
                activeSubTaskCount++;
                subTask.setParentTask(this);
            }
        }
    }

    @Override
    public void setTaskContext(IEntityContext context) {
        this.taskContext = context;
    }

    @Override
    protected void onScanOver(IEntityContext context) {
        if(activeSubTaskCount <= 0) {
            commit();
        }
    }

    public void commit() {
        commit.finish();
    }

    @Override
    public WAL getWAL() {
        return commit.getWal();
    }

    @Override
    public boolean isMigrationDisabled() {
        return true;
    }

    @Override
    public void onSubTaskFinished(Task task) {
        if(--activeSubTaskCount <= 0 && isFinished()) {
            commit();
        }
    }

    @Override
    public int getActiveSubTaskCount() {
        return activeSubTaskCount;
    }
}
