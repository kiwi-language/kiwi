package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.DefaultTypeFactory;
import org.metavm.object.type.TypeFactory;

import javax.annotation.Nullable;

public class CompilerEntityContext extends BaseEntityContext {

    private final DefContext defContext;

    public CompilerEntityContext(IInstanceContext instanceContext, @Nullable IEntityContext parent, DefContext defContext) {
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
    public IEntityContext createSame(long appId) {
        return new CompilerEntityContext(
                getInstanceContext(),
                getParent(),
                getDefContext()
        );
    }

}
