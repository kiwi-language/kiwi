package org.metavm.task;

import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

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
