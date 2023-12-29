package tech.metavm.task;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;

public abstract class ScanByTypeTask extends ScanTask {

    @EntityField("类型")
    protected final Type type;

    protected ScanByTypeTask(String title, Type type) {
        super(title);
        this.type = type;
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, DurableInstance cursor, long limit) {
        return context.getByType(type, cursor, limit);
    }

    @Override
    protected final void process(List<DurableInstance> batch, IEntityContext context) {
        for (Instance instance : batch) {
            processInstance(instance, context.getInstanceContext());
        }
    }

    protected abstract void processInstance(Instance instance, IInstanceContext context);
}
