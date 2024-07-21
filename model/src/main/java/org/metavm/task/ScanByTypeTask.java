package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceReference;
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
    protected List<InstanceReference> scan(IInstanceContext context, long cursor, long limit) {
        return NncUtils.filter(context.scan(cursor, limit), this::filer);
    }

    private boolean filer(InstanceReference instance) {
        Klass klass;
        if(type instanceof ClassType classType && (klass = classType.resolve()).isTemplate()) {
            if(instance.resolve() instanceof ClassInstance classInstance)
                return classInstance.getKlass().findAncestorByTemplate(klass) != null;
            else
                return false;
        }
        else
            return type.isInstance(instance);
    }

    @Override
    protected final void process(List<InstanceReference> batch, IEntityContext context, IEntityContext taskContext) {
        for (Instance instance : batch) {
            processInstance(instance, context);
        }
    }

    protected abstract void processInstance(Instance instance, IEntityContext context);
}
