package tech.metavm.mocks;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.job.Job;

@EntityType("测试任务")
public class TestJob extends Job {

    private int count = 0;

    public TestJob() {
        super("Test Job");
    }

    @Override
    protected boolean run0(IInstanceContext context) {
        return ++count >= 10;
    }

    public int getCount() {
        return count;
    }
}
