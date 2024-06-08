package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType
public abstract class ScanByTypeTask extends ScanTask {

    protected final Type type;

    protected ScanByTypeTask(String title, Type type) {
        super(title);
        this.type = type;
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, long cursor, long limit) {
        return NncUtils.filter(context.scan(cursor, limit), type::isInstance);
    }

    @Override
    protected final void process(List<DurableInstance> batch, IEntityContext context) {
        for (Instance instance : batch) {
            processInstance(instance, context.getInstanceContext());
        }
    }

    protected abstract void processInstance(Instance instance, IInstanceContext context);
}
