package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;

import javax.annotation.Nullable;

public class CompilerEntityContext extends BaseEntityContext {

    private final DefContext defContext;

    public CompilerEntityContext(IInstanceContext instanceContext, DefContext defContext) {
        super(instanceContext);
        this.defContext = defContext;
    }

    @Override
    public DefContext getDefContext() {
        return defContext;
    }

    @Override
    public IEntityContext createSame(long appId) {
        return new CompilerEntityContext(
                getInstanceContext(),
                getDefContext()
        );
    }

}
