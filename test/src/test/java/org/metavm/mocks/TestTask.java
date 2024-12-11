package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.task.Task;

@Entity
public class TestTask extends Task {

    private int count = 0;

    public TestTask() {
        super("Test Job");
    }

    @Override
    protected boolean run0(IEntityContext context, IEntityContext taskContext) {
        return ++count >= 10;
    }

    public int getCount() {
        return count;
    }
}
