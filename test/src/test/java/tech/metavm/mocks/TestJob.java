package tech.metavm.mocks;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.task.Task;

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
