package org.metavm.task;

import org.metavm.entity.EntityUtils;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.util.Utils;

public class FieldRemovalTask {

    public static final long BATCH_SIZE = 100;

    private final Field field;

    private long cursor;

    public FieldRemovalTask(Field field) {
        this.field = field;
    }

    public boolean executeBatch(IInstanceContext context) {
        var instances = context.scan(cursor, BATCH_SIZE).instances();
        if(Utils.isEmpty(instances)) {
            doFinally(context);
            return true;
        }
        instances.stream().map(Instance::getReference).filter(field.getDeclaringType().getType()::isInstance)
                .forEach(this::processInstance);
        cursor = instances.get(instances.size() - 1).getTreeId();
        return false;
    }

    private void doFinally(IInstanceContext context) {
        context.remove(field);
    }

    private void processInstance(Value instance) {
        EntityUtils.ensureProxyInitialized(instance);
    }

}
