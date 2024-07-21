package org.metavm.task;

import org.metavm.entity.IEntityContext;

public class DDLRollbackTask extends Task {

    protected DDLRollbackTask() {
        super("DDLRollbackTask");
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        return true;
    }
}
