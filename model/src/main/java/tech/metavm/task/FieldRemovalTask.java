package tech.metavm.task;

import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;
import tech.metavm.util.NncUtils;

//@EntityType("字段删除任务")
public class FieldRemovalTask {

    public static final long BATCH_SIZE = 100;

    private final Field field;

    private long cursor;

    public FieldRemovalTask(Field field) {
        this.field = field;
    }

    public boolean executeBatch(IEntityContext context) {
        var instances = context.getInstanceContext().scan(cursor, BATCH_SIZE);
        if(NncUtils.isEmpty(instances)) {
            doFinally(context);
            return true;
        }
        instances.stream().filter(field.getDeclaringType().getType()::isInstance)
                .forEach(this::processInstance);
        cursor = instances.get(instances.size() - 1).getTreeId();
        return false;
    }

    private void doFinally(IEntityContext context) {
        context.remove(field);
    }

    private void processInstance(Instance instance) {
        EntityUtils.ensureProxyInitialized(instance);
    }

}
