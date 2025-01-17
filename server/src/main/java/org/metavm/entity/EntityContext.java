package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.CompositeTypeFactory;
import org.metavm.object.type.Type;

public class EntityContext extends BaseEntityContext implements CompositeTypeFactory, IEntityContext {

    private final DefContext defContext;

    public EntityContext(IInstanceContext instanceContext) {
        this(instanceContext, ModelDefRegistry.getDefContext());
    }

    public EntityContext(IInstanceContext instanceContext, DefContext defContext) {
        super(instanceContext);
        this.defContext = defContext;
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
        return new EntityContext(getInstanceContext().createSame(appId));
    }
}