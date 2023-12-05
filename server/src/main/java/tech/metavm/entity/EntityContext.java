package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.type.CompositeTypeFactory;
import tech.metavm.object.type.DefaultTypeFactory;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeFactory;

public class EntityContext extends BaseEntityContext implements CompositeTypeFactory, IEntityContext {

    private final DefContext defContext;

    public EntityContext(IInstanceContext instanceContext, IEntityContext parent) {
        this(instanceContext, parent, ModelDefRegistry.getDefContext());
    }

    public EntityContext(IInstanceContext instanceContext, IEntityContext parent,
                         DefContext defContext) {
        super(instanceContext, parent);
        this.defContext = defContext;
    }

    @Override
    protected TypeFactory getTypeFactory() {
        return new DefaultTypeFactory(ModelDefRegistry::getType);
    }

    @Override
    public DefContext getDefContext() {
        return defContext;
    }

    @Override
    public Type getType(Class<?> javaType) {
        return defContext.getType(javaType);
    }

    @Override
    public IEntityContext createSame(long appId) {
        //noinspection resource
        return getInstanceContext().createSame(appId).getEntityContext();
    }
}