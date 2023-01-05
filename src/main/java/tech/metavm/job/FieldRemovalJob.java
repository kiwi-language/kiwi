package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.List;

//@EntityType("字段删除任务")
public class FieldRemovalJob {

    public static final long BATCH_SIZE = 100;

    private final Field field;
    private Instance cursor;

    public FieldRemovalJob(Field field) {
        this.field = field;
    }

    public boolean executeBatch(IInstanceContext context) {
        List<Instance> instances = context.getByType(field.getDeclaringType(), cursor, BATCH_SIZE);
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

    private void doFinally(IInstanceContext context) {
        context.getEntityContext().remove(field);
    }

    private void processInstance(Instance instance) {
        EntityUtils.ensureProxyInitialized(instance);
    }

}
