package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.ddl.Commit;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.Field;
import org.metavm.object.type.MetadataState;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DDL extends ScanTask {

    public static DDL create(Commit commit, IEntityContext context) {
        var fields = NncUtils.mapAndFilterByType(
                commit.getNewElementIds(),
                id -> context.getEntity(Object.class, id),
                Field.class
        );
        return new DDL(fields, commit);
    }

    private static final Logger logger = LoggerFactory.getLogger(DDL.class);

    @ChildEntity
    private final ReadWriteArray<Field> fields = addChild(new ReadWriteArray<>(Field.class), "fields");
    private final Commit commit;

    public DDL(List<Field> fields, Commit commit) {
        super("DDL " + NncUtils.formatDate(commit.getTime()));
        this.fields.addAll(fields);
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
        for (Field field : fields) {
            if (field.getDeclaringType().getType().isInstance(instance))
                initializeField(instance, field, context);
        }
    }

    private void initializeField(ClassInstance instance, Field field, IEntityContext context) {
        var initialValue = Instances.computeFieldInitialValue(instance, field, context.getInstanceContext());
        if (!instance.isFieldInitialized(field))
            instance.initField(field, initialValue);
    }

    @Override
    protected void onScanOver(IEntityContext context) {
        for (Field field : fields) {
            field.setState(MetadataState.READY);
        }
        Types.submitCommit(commit, context);
        commit.finish();
    }

}
