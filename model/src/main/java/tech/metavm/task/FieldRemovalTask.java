package tech.metavm.task;

import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

//@EntityType("字段删除任务")
public class FieldRemovalTask {

    public static final long BATCH_SIZE = 100;

    private final Field field;
    @Nullable
    private DurableInstance cursor;

    public FieldRemovalTask(Field field) {
        this.field = field;
    }

    public boolean executeBatch(IEntityContext context) {
        var instances = context.getInstanceContext().getByType(field.getDeclaringType(), cursor, BATCH_SIZE);
        if(NncUtils.isEmpty(instances)) {
            doFinally(context);
            return true;
        }
        instances.forEach(this::processInstance);
        cursor = instances.get(instances.size() - 1);
        if(instances.size() < BATCH_SIZE) {
            doFinally(context);
            return true;
        }
        else {
            return false;
        }
    }

    private void doFinally(IEntityContext context) {
        context.remove(field);
    }

    private void processInstance(Instance instance) {
        EntityUtils.ensureProxyInitialized(instance);
    }

}
