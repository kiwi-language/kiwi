package org.metavm.mocks;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.task.Task;

@EntityType
public class TestTask extends Task {

    private int count = 0;

    public TestTask() {
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
