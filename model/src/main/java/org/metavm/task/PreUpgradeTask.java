package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.ddl.FieldAddition;
import org.metavm.ddl.SystemDDL;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class PreUpgradeTask extends ScanTask  implements WalTask {

    @ChildEntity
    private final WAL wal;
    private final String ddlId;

    public PreUpgradeTask(WAL wal, String ddlId) {
        super("PreUpgradeTask-" + ddlId);
        this.wal = wal;
        this.ddlId = ddlId;
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        var instCtx = context.getInstanceContext();
        var ddl = context.getEntity(SystemDDL.class, ddlId);
        for (FieldAddition field : ddl.getFieldAdditions()) {
            var klass = field.klass();
            for (Instance instance : batch) {
                if(instance instanceof ClassInstance clsInst) {
                    var k = clsInst.getKlass().findAncestorByTemplate(klass);
                    if(k != null) {
                        var value = Flows.invoke(field.initializer(), null, List.of(clsInst.getReference()), instCtx);
                        clsInst.setFieldByTag(k.getTag(), field.fieldTag(), value);
                        clsInst.setDirectlyModified(true);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return wal;
    }

}
