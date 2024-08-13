package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.ddl.FieldAddition;
import org.metavm.ddl.SystemDDL;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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

    @Override
    protected void onScanOver(IEntityContext context, IEntityContext taskContext) {
        var newKlassIds = getExtraStdKlassIds();
        for (String klassId : newKlassIds) {
            var klass = context.getKlass(klassId);
            var initializer = tryGetInitializerKlass(klass, context);
            if (initializer != null) {
                var creationMethod = initializer.findMethod(m -> m.isStatic() && m.getName().equals("create")
                        && m.getParameters().isEmpty() && m.getReturnType().equals(klass.getType()));
                if (creationMethod != null) {
                    Objects.requireNonNull(Flows.invoke(creationMethod, null, List.of(), context));
                }
            }
        }
    }

    private Klass tryGetInitializerKlass(Klass klass, IEntityContext context) {
        return context.selectFirstByKey(Klass.UNIQUE_CODE, klass.getCodeNotNull() + "Initializer");
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return wal;
    }

    @Override
    public long getTimeout() {
        return 1500L;
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
