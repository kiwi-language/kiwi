package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.Type;
import org.metavm.util.Utils;

import java.util.List;
import java.util.function.Consumer;

@Wire(50)
@Entity
public abstract class ScanByTypeTask extends ScanTask {

    protected Type type;

    protected ScanByTypeTask(Id id, String title, Type type) {
        super(id, title);
        this.type = type;
    }

    @Override
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        var r = context.scan(cursor, limit);
        return new ScanResult(Utils.filter(r.instances(), this::filter), r.completed(), r.cursor());
    }

    private boolean filter(Instance instance) {
        if(type instanceof KlassType classType && classType.isTemplate()) {
            if(instance instanceof ClassInstance classInstance)
                return classInstance.getInstanceType().asSuper(classType.getKlass()) != null;
            else
                return false;
        }
        else
            return type.isInstance(instance.getReference());
    }

    @Override
    protected final void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        for (var instance : batch) {
            processInstance(instance.getReference(), context);
        }
    }

    protected abstract void processInstance(Value instance, IInstanceContext context);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
