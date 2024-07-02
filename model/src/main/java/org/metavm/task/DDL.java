package org.metavm.task;

import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.Field;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DDL extends ScanTask implements WalTask {

    private static final Logger logger = LoggerFactory.getLogger(DDL.class);

    private final Commit commit;

    public DDL(Commit commit) {
        super("DDL " + NncUtils.formatDate(commit.getTime()));
        this.commit = commit;
    }

    @Override
    protected List<DurableInstance> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context) {
        batch.forEach(i -> {
            if (i instanceof ClassInstance classInstance)
                processOne(classInstance, context);
        });
    }

    private void processOne(ClassInstance instance, IEntityContext context) {
        var newFields = NncUtils.map(commit.getNewFieldIds(), context::getField);
        for (Field field : newFields) {
            if (field.getDeclaringType().getType().isInstance(instance))
                initializeField(instance, field, context);
        }
        var convertingFields = NncUtils.map(commit.getConvertingFieldIds(), context::getField);
        for (Field field : convertingFields) {
            if (field.getDeclaringType().getType().isInstance(instance))
                convertField(instance, field, context);
        }
    }

    private void initializeField(ClassInstance instance, Field field, IEntityContext context) {
        var initialValue = Instances.computeFieldInitialValue(instance, field, context.getInstanceContext());
        instance.setField(field, initialValue);
    }

    private void convertField(ClassInstance instance, Field field, IEntityContext context) {
        var convertedValue = Instances.computeConvertedFieldValue(instance, field, context.getInstanceContext());
        instance.setField(field, convertedValue);
        logger.info("Converted value for " + field.getQualifiedName() + ": " + instance.getField(field));
    }

    @Override
    protected void onScanOver(IEntityContext context) {
//        var convertingFields = NncUtils.map(commit.getConvertingFieldIds(), context::getField);
//        for (Field field : convertingFields) {
//            field.clearOriginalTag();
//        }
        commit.finish();
    }

    @Override
    public WAL getWAL() {
        return commit.getWal();
    }
}
