package org.metavm.task;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.ddl.FieldAddition;
import org.metavm.ddl.SystemDDL;
import org.metavm.entity.EntityRegistry;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@NativeEntity(39)
@Entity
public class PreUpgradeTask extends ScanTask  {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference walReference;
    private Id ddlId;
    private Id defWalId;
    private List<Id> newKlassIds = new ArrayList<>();

    public PreUpgradeTask(Id id, WAL wal, Id defWalId, List<Id> newKlassIds, Id ddlId) {
        super(id, "PreUpgradeTask-" + ddlId);
        this.walReference = wal.getReference();
        this.defWalId = defWalId;
        this.ddlId = ddlId;
        this.newKlassIds.addAll(newKlassIds);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        ScanTask.visitBody(visitor);
        visitor.visitValue();
        visitor.visitId();
        visitor.visitId();
        visitor.visitList(visitor::visitId);
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        for (Instance instance : batch) {
            if(instance instanceof ClassInstance clsInst) {
                var ddl = context.getEntity(SystemDDL.class, ddlId);
                for (FieldAddition fieldAdd : ddl.getFieldAdditions()) {
                    var field  = fieldAdd.field();
                    var klass = field.getDeclaringType();
                    var k = clsInst.getInstanceType().asSuper(klass);
                    if(k != null) {
                        var value = Flows.invoke(fieldAdd.initializer().getRef(), null, List.of(clsInst.getReference()), context);
                        clsInst.setFieldForce(field, value);
                    }
                }
                for (Method runMethod : ddl.getRunMethods()) {
                    var paramKlass = ((ClassType) runMethod.getParameterTypes().getFirst());
                    var k = clsInst.getInstanceType().asSuper(paramKlass.getKlass());
                    if(k != null) {
                        var pm = runMethod.getTypeParameters().isEmpty() ? runMethod.getRef() :
                                runMethod.getRef().getParameterized(List.of(clsInst.getInstanceType()));
                        Flows.invoke(pm, null, List.of(clsInst.getReference()), context);
                    }
                }
            }
        }
    }

    @Override
    protected void onScanOver(IInstanceContext context, IInstanceContext taskContext) {
        var newKlassIds = getExtraStdKlassIds();
        for (var klassId : newKlassIds) {
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

    private Klass tryGetInitializerKlass(Klass klass, IInstanceContext context) {
        return context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME,
                Instances.stringInstance(klass.getQualifiedName() + "Initializer"));
    }

    @Nullable
    @Override
    public WAL getWAL() {
        return (WAL) walReference.get();
    }

    @Override
    public long getTimeout() {
        return 10000L;
    }

    @Nullable
    @Override
    public Id getDefWalId() {
        return defWalId;
    }

    @Override
    public List<Id> getExtraStdKlassIds() {
        return Collections.unmodifiableList(newKlassIds);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(walReference);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        var wAL = this.getWAL();
        if (wAL != null) map.put("wAL", wAL.getStringId());
        map.put("timeout", this.getTimeout());
        var defWalId = this.getDefWalId();
        if (defWalId != null) map.put("defWalId", defWalId);
        map.put("extraStdKlassIds", this.getExtraStdKlassIds());
        var group = this.getGroup();
        if (group != null) map.put("group", group.getStringId());
        map.put("runCount", this.getRunCount());
        map.put("state", this.getState().name());
        map.put("runnable", this.isRunnable());
        map.put("running", this.isRunning());
        map.put("completed", this.isCompleted());
        map.put("failed", this.isFailed());
        map.put("terminated", this.isTerminated());
        map.put("lastRunTimestamp", this.getLastRunTimestamp());
        map.put("startAt", this.getStartAt());
        var metaWAL = this.getMetaWAL();
        if (metaWAL != null) map.put("metaWAL", metaWAL.getStringId());
        map.put("relocationEnabled", this.isRelocationEnabled());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_PreUpgradeTask;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.walReference = (Reference) input.readValue();
        this.ddlId = input.readId();
        this.defWalId = input.readId();
        this.newKlassIds = input.readList(input::readId);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(walReference);
        output.writeId(ddlId);
        output.writeId(defWalId);
        output.writeList(newKlassIds, output::writeId);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}
