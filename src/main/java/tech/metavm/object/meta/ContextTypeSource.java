package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;

public class ContextTypeSource implements TypeSource {

    private final IEntityContext context;

    public ContextTypeSource(IEntityContext context) {
        this.context = context;
    }

    @Override
    public Type getType(String name) {
        return context.selectByUniqueKey(Type.UNIQUE_NAME, name);
    }

    @Override
    public void addType(Type type) {
        context.bind(type);
    }

}
