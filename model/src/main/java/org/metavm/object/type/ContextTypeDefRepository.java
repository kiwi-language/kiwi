package org.metavm.object.type;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.Id;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

public class ContextTypeDefRepository implements TypeDefRepository {

    private final IEntityContext context;

    public ContextTypeDefRepository(IEntityContext context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Klass findKlassByName(String name) {
        return context.selectFirstByKey(Klass.IDX_NAME, Instances.stringInstance(name));
    }

    @Override
    public ITypeDef getTypeDef(Id id) {
        return context.getEntity(ITypeDef.class, id);
    }

    @Override
    public void save(TypeDef typeDef) {
        context.bind(typeDef);
    }
}
