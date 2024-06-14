package org.metavm.mocks;

import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.task.Task;

@EntityType
public class TestJob extends Task {

    private int count = 0;

    public TestJob() {
        super("Test Job");
    }

    @Override
    protected boolean run0(IEntityContext context) {
        return ++count >= 10;
    }

    public int getCount() {
        return count;
    }
}
