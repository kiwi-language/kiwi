package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.ddl.FieldAddition;
import org.metavm.ddl.SystemDDL;
import org.metavm.entity.ContextFlag;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class PreUpgradeTask extends ScanTask {

    @ChildEntity
    private final WAL wal;
    private final String ddlId;
    @ChildEntity
    private final WAL defWal;
    @ChildEntity
    private final ReadWriteArray<String> newKlassIds = addChild(new ReadWriteArray<>(String.class), "newKlassIds");

    public PreUpgradeTask(WAL wal, WAL defWal, List<String> newKlassIds, String ddlId) {
        super("PreUpgradeTask-" + ddlId);
        this.wal = wal;
        this.defWal = defWal;
        this.ddlId = ddlId;
        this.newKlassIds.addAll(newKlassIds);
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        var instCtx = context.getInstanceContext();
        var ddl = context.getEntity(SystemDDL.class, ddlId);
        for (FieldAddition fieldAdd : ddl.getFieldAdditions()) {
            var field  = fieldAdd.field();
            var klass = field.getDeclaringType();
            for (Instance instance : batch) {
                if(instance instanceof ClassInstance clsInst) {
                    var k = clsInst.getKlass().findAncestorByTemplate(klass);
                    if(k != null) {
                        var value = Flows.invoke(fieldAdd.initializer(), null, List.of(clsInst.getReference()), instCtx);
                        clsInst.setFieldForce(field, value);
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

    @Override
    public long getTimeout() {
        return 1000L;
    }

    @Nullable
    @Override
    public WAL getDefWAL() {
        return defWal;
    }

    @Override
    public List<String> getExtraStdKlassIds() {
        return newKlassIds.toList();
    }

}
