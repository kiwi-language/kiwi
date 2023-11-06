package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.meta.*;

import java.util.Set;

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
    protected TypeFactory getTypeFactory() {
        return new DefaultTypeFactory(ModelDefRegistry::getType);
    }

    @Override
    public DefContext getDefContext() {
        return defContext;
    }

    @Override
    protected boolean manualInstanceWriting() {
        return false;
    }

    @Override
    public UnionType getNullableType(Type type) {
        return getUnionType(Set.of(type, StandardTypes.getNullType()));
    }

    @Override
    public Type getType(Class<?> javaType) {
        return defContext.getType(javaType);
    }

    @Override
    public boolean isBindSupported() {
        return true;
    }
}