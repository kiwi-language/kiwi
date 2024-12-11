package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.ddl.FieldAddition;
import org.metavm.ddl.SystemDDL;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.instance.log.Identifier;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Entity
public class PreUpgradeTask extends ScanTask {

    @ChildEntity
    private final WAL wal;
    private final String ddlId;
    private final Identifier defWalId;
    @ChildEntity
    private final ReadWriteArray<String> newKlassIds = addChild(new ReadWriteArray<>(String.class), "newKlassIds");

    public PreUpgradeTask(WAL wal, Identifier defWalId, List<String> newKlassIds, String ddlId) {
        super("PreUpgradeTask-" + ddlId);
        this.wal = wal;
        this.defWalId = defWalId;
        this.ddlId = ddlId;
        this.newKlassIds.addAll(newKlassIds);
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        var instCtx = context.getInstanceContext();
        for (Instance instance : batch) {
            if(instance instanceof ClassInstance clsInst) {
                var ddl = context.getEntity(SystemDDL.class, ddlId);
                for (FieldAddition fieldAdd : ddl.getFieldAdditions()) {
                    var field  = fieldAdd.field();
                    var klass = field.getDeclaringType();
                    var k = clsInst.getType().findAncestorByKlass(klass);
                    if(k != null) {
                        var value = Flows.invoke(fieldAdd.initializer().getRef(), null, List.of(clsInst.getReference()), instCtx);
                        clsInst.setFieldForce(field, value);
                        clsInst.setDirectlyModified(true);
                    }
                }
                for (Method runMethod : ddl.getRunMethods()) {
                    var paramKlass = ((ClassType) runMethod.getParameterTypes().get(0));
                    var k = clsInst.getType().findAncestorByKlass(paramKlass.getKlass());
                    if(k != null) {
                        var pm = runMethod.getTypeParameters().isEmpty() ? runMethod.getRef() :
                                runMethod.getRef().getParameterized(List.of(clsInst.getType()));
                        Flows.invoke(pm, null, List.of(clsInst.getReference()), context);
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
                    Objects.requireNonNull(Flows.invoke(creationMethod.getRef(), null, List.of(), context));
                }
            }
        }
    }

    private Klass tryGetInitializerKlass(Klass klass, IEntityContext context) {
        return context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, klass.getQualifiedName() + "Initializer");
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return wal;
    }

    @Override
    public long getTimeout() {
        return 10000L;
    }

    @Nullable
    @Override
    public Identifier getDefWalId() {
        return defWalId;
    }

    @Override
    public List<String> getExtraStdKlassIds() {
        return newKlassIds.toList();
    }

}
