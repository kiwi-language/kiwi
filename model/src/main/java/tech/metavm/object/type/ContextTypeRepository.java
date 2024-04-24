package tech.metavm.object.type;

import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.core.Id;

public class ContextTypeRepository implements TypeRepository {

    private final IEntityContext context;

    public ContextTypeRepository(IEntityContext context) {
        this.context = context;
    }

    @Override
    public Klass findClassTypeByName(String name) {
        return context.selectFirstByKey(Klass.IDX_NAME, name);
    }

    @Override
    public Type getType(Id id) {
        return context.getType(id);
    }

    @Override
    public void save(Type type) {
        context.bind(type);
    }
}
