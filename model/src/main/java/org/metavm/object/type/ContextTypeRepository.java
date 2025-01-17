package org.metavm.object.type;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Id;
import org.metavm.util.Instances;

public class ContextTypeRepository implements TypeRepository {

    private final IEntityContext context;

    public ContextTypeRepository(IEntityContext context) {
        this.context = context;
    }

    @Override
    public Klass findClassTypeByName(String name) {
        return context.selectFirstByKey(Klass.IDX_NAME, Instances.stringInstance(name));
    }

    @Override
    public Type getType(Id id) {
        return context.getType(id);
    }

}
