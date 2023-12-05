package tech.metavm.instance.core;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.*;
import tech.metavm.event.EventQueue;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.instance.core.BufferingInstanceContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public class CompilerInstanceContext extends BufferingInstanceContext {

    private IEntityContext entityContext;

    public CompilerInstanceContext(long appId,
                                   List<TreeSource> treeSources,
                                   VersionSource versionSource,
                                   EntityIdProvider idService,
                                   IndexSource indexSource,
                                   DefContext defContext, IInstanceContext parent, boolean readonly) {
        super(appId,
                treeSources, versionSource,
                idService,
                indexSource,
                defContext,
                parent, readonly);
        entityContext = new CompilerEntityContext(this,
                NncUtils.get(parent, IInstanceContext::getEntityContext), defContext);
    }

    @Override
    public IInstanceContext createSame(long appId) {
        return null;
    }

    @Override
    public List<Instance> getByType(Type type, Instance startExclusive, long limit) {
        return null;
    }

    @Override
    public List<Instance> scan(Instance startExclusive, long limit) {
        return null;
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        return false;
    }

    @Override
    public IEntityContext getEntityContext() {
        return entityContext;
    }

    @Override
    public List<Instance> getByReferenceTargetId(long targetId, Instance startExclusive, long limit) {
        return null;
    }

    @Override
    protected void finishInternal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void initIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType(long id) {
        return entityContext.getType(id);
    }

    @Nullable
    @Override
    public EventQueue getEventQueue() {
        return null;
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }
}
