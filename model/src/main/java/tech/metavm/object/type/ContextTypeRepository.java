package tech.metavm.object.type;

import tech.metavm.common.RefDTO;
import tech.metavm.entity.IEntityContext;

public class ContextTypeRepository implements TypeRepository {

    private final IEntityContext context;

    public ContextTypeRepository(IEntityContext context) {
        this.context = context;
    }

    @Override
    public ClassType findClassTypeByName(String name) {
        return context.selectByUniqueKey(ClassType.UNIQUE_NAME, name);
    }

    @Override
    public Type getType(RefDTO ref) {
        return context.getType(ref);
    }

    @Override
    public void save(Type type) {
        context.bind(type);
    }
}
