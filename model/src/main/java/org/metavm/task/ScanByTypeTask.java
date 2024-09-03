package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;

@EntityType
public abstract class ScanByTypeTask extends ScanTask {

    protected final Type type;

    protected ScanByTypeTask(String title, Type type) {
        super(title);
        this.type = type;
    }

    @Override
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        var r = context.scan(cursor, limit);
        return new ScanResult(NncUtils.filter(r.instances(), this::filer), r.completed(), r.cursor());
    }

    private boolean filer(Instance instance) {
        Klass klass;
        if(type instanceof ClassType classType && (klass = classType.resolve()).isTemplate()) {
            if(instance instanceof ClassInstance classInstance)
                return classInstance.getKlass().findAncestorByTemplate(klass) != null;
            else
                return false;
        }
        else
            return type.isInstance(instance.getReference());
    }

    @Override
    protected final void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
        for (var instance : batch) {
            processInstance(instance.getReference(), context);
        }
    }

    protected abstract void processInstance(Value instance, IEntityContext context);
}
