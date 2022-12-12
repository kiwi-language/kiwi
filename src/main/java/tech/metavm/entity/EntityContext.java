package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.meta.*;

public class EntityContext extends BaseEntityContext implements CompositeTypeFactory, IEntityContext {

    private final DefContext defContext;

    public EntityContext(@Nullable IInstanceContext instanceContext, IEntityContext parent) {
        this(instanceContext, parent, ModelDefRegistry.getDefContext());
    }

    public EntityContext(@Nullable IInstanceContext instanceContext, IEntityContext parent, DefContext defContext) {
        super(instanceContext, parent);
        this.defContext = defContext;
    }

    @Override
    protected DefContext getDefContext() {
        return defContext;
    }

    @Override
    protected boolean lateBinding() {
        return false;
    }

}