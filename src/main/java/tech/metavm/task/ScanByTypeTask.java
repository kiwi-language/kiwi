package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;

import java.util.List;

public abstract class ScanByTypeTask extends ScanTask {

    @EntityField("类型")
    protected final Type type;

    protected ScanByTypeTask(String title, Type type) {
        super(title);
        this.type = type;
    }

    @Override
    protected List<Instance> scan(IInstanceContext context, Instance cursor, long limit) {
        return context.getByType(type, cursor, limit);
    }

    @Override
    protected final void process(List<Instance> batch, IInstanceContext context) {
        for (Instance instance : batch) {
            processInstance(instance, context);
        }
    }

    protected abstract void processInstance(Instance instance, IInstanceContext context);
}
